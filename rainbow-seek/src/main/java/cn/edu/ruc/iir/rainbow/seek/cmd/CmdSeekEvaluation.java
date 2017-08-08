package cn.edu.ruc.iir.rainbow.seek.cmd;

import cn.edu.ruc.iir.rainbow.common.cmd.ProgressListener;
import cn.edu.ruc.iir.rainbow.common.cmd.Receiver;
import cn.edu.ruc.iir.rainbow.common.exception.ExceptionHandler;
import cn.edu.ruc.iir.rainbow.common.exception.ExceptionType;
import cn.edu.ruc.iir.rainbow.common.exception.ParameterNotSupportedException;
import cn.edu.ruc.iir.rainbow.common.cmd.Command;
import cn.edu.ruc.iir.rainbow.common.util.HDFSInputFactory;
import cn.edu.ruc.iir.rainbow.seek.SeekPerformer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by hank on 16-12-25.
 */
public class CmdSeekEvaluation implements Command
{
    private SeekPerformer seekPerformer = null;
    private Receiver receiver = null;

    public CmdSeekEvaluation()
    {
        this.seekPerformer = SeekPerformer.Instance();
    }

    @Override
    public void setReceiver(Receiver receiver)
    {
        this.receiver = receiver;
    }

    /**
     * params should contain the following settings:
     * <ol>
     *   <li>method, HDFS or LOCAL</li>
     *   <li>data.path, the directory on HDFS to store the generated files if method=HDFS</li>
     *   <li>num.seeks, number of seeks to be performed in each segment</li>
     *   <li>seek.distance, the distance of each seek</li>
     *   <li>read.length,  the length in bytes been read after each seek</li>
     *   <li>skip.file.num, the number of files been skipped between two segments if method=HDFS</li>
     *   <li>skip.distance, the distance in bytes been skipped between two segments if method=LOCAL</li>
     *   <li>log.dir, the local directory used to write evaluation results.
     *   This directory can not be exist and will be created by this method itself</li>
     *   <li>start.num, the integer number of HDFS file the seek evaluation starts if method=HDFS</li>
     *   <li>end.num, the integer number of HDFS file the seek evaluation ends if method=HDFS</li>
     *   <li>start.offset, the offset in bytes the seek evaluation starts if method=LOCAL</li>
     *   <li>end.offset, the offset in bytes the seek evaluation ends if method=LOCAL</li>
     * </ol>
     * this method will pass the following results to receiver:
     * <ol>
     *   <li>success, true or false</li>
     *   <li>log.dir</li>
     * </ol>
     * @param params
     */
    public void execute(Properties params)
    {
        Properties results = new Properties(params);
        results.setProperty("success", "false");
        ProgressListener progressListener = percentage -> {
            if (this.receiver != null)
            {
                this.receiver.progress(percentage);
            }
        };
        progressListener.setPercentage(0.0);

        // test the seek cost
        if (params.getProperty("method") == null)
        {
            ExceptionHandler.Instance().log(ExceptionType.ERROR, "method is null. Exiting...",
                    new NullPointerException());
            if (receiver != null)
            {
                receiver.action(results);
            }
            return;
        }
        String path = params.getProperty("data.path");
        int numSeeks = Integer.parseInt(params.getProperty("num.seeks"));
        long seekDistance = Long.parseLong(params.getProperty("seek.distance"));
        int readLength = Integer.parseInt(params.getProperty("read.length"));
        String logDir = params.getProperty("log.dir");
        if (logDir.charAt(logDir.length()-1) != '/' && logDir.charAt(logDir.length()-1) != '\\')
        {
            logDir += "/";
        }
        File dir = new File(logDir);
        if (dir.exists())
        {
            ExceptionHandler.Instance().log(ExceptionType.ERROR, "log directory " + logDir +
                    " exists. Exiting...", new ParameterNotSupportedException(logDir));
            if (receiver != null)
            {
                receiver.action(results);
            }
            return;
        }
        dir.mkdirs();
        if (params.getProperty("method").equalsIgnoreCase("hdfs"))
        {
            //test hdfs seek cost
            int skipFileNum = Integer.parseInt(params.getProperty("skip.file.num"));
            Configuration conf  = new Configuration();
            try
            {
                FileStatus[] statuses = HDFSInputFactory.Instance().getFileStatuses(path, conf);
                BufferedWriter writer = new BufferedWriter(new FileWriter(logDir + "aggregate"));
                writer.write("fileNumber\tseekDistance\treadSize\tfileSeekTime(us)\n");
                System.out.print((seekDistance / 1024));
                int startFileNumber = 0, endFileNumber = statuses.length;
                if (params.getProperty("start.num") != null &&
                        params.getProperty("end.num") != null)
                {
                    startFileNumber = Integer.parseInt(params.getProperty("start.num"));
                    endFileNumber = Integer.parseInt(params.getProperty("end.num"));
                }

                int totalFileNumber = (endFileNumber - startFileNumber) / skipFileNum;
                int processedFileNumber = 0;
                for (int i = startFileNumber; i < endFileNumber; i += skipFileNum)
                {
                    // get the hdfs file status
                    FileStatus status = statuses[i];
                    long[] seekCosts = new long[numSeeks];
                    int realNumSeeks = seekPerformer.performHDFSSeeks(
                            seekCosts, status, numSeeks, readLength, seekDistance, conf);
                    double fileMidCost = seekPerformer.logSeekCost(seekCosts, realNumSeeks, logDir + "details-of-file-" + i);
                    writer.write(i + "\t" + seekDistance + "\t" + readLength + "\t" + fileMidCost + "\n");
                    System.out.print("\t" + fileMidCost);
                    processedFileNumber ++;
                    progressListener.setPercentage(1.0 * processedFileNumber / totalFileNumber);
                }
                writer.flush();
                writer.close();
                System.out.println();

            } catch (IOException e)
            {
                ExceptionHandler.Instance().log(ExceptionType.ERROR, "hdfs seek evaluation error", e);
            }
            results.setProperty("success", "true");
        } else if (params.getProperty("method").equalsIgnoreCase("local"))
        {
            //test local seek cost
            long skipDistance = Long.parseLong(params.getProperty("skip.distance"));

            try
            {
                File file = new File(path);
                long startOffset = 0, endOffset = file.length();
                BufferedWriter writer = new BufferedWriter(new FileWriter(logDir + "aggregate"));
                writer.write("initPos\tseekDistance\treadSize\tsegSeekTime(us)\n");
                System.out.print((seekDistance / 1024));
                if (params.getProperty("start.offset") != null &&
                        params.getProperty("end.offset") != null)
                {
                    startOffset = Long.parseLong(params.getProperty("start.offset"));
                    endOffset = Long.parseLong(params.getProperty("end.offset"));
                }
                long totalSegmentNumber = (endOffset - startOffset) / skipDistance;
                long processedSegmentNumber = 0;
                while (startOffset < endOffset)
                {
                    long[] seekCosts = new long[numSeeks];
                    int realNumSeeks = seekPerformer.performLocalSeeks(
                            seekCosts, file, startOffset, numSeeks, readLength, seekDistance);
                    double segMidCost = seekPerformer.logSeekCost(seekCosts, realNumSeeks, logDir + "details-of-seg-" + startOffset);
                    writer.write(startOffset + "\t" + seekDistance + "\t" + readLength + "\t" + segMidCost + "\n");

                    System.out.print("\t" + segMidCost);
                    startOffset += skipDistance;
                    processedSegmentNumber++;
                    progressListener.setPercentage(1.0 * processedSegmentNumber / totalSegmentNumber);
                }
                writer.flush();
                writer.close();
                System.out.println();

            } catch (IOException e)
            {
                ExceptionHandler.Instance().log(ExceptionType.ERROR, "local seek evaluation error", e);
            }
            results.setProperty("success", "true");
        } else
        {
            ExceptionHandler.Instance().log(ExceptionType.ERROR, params.getProperty("method") +
                    " not supported. Exiting...", new ParameterNotSupportedException(params.getProperty("method")));
        }

        if (receiver != null)
        {
            receiver.action(results);
        }
    }
}
