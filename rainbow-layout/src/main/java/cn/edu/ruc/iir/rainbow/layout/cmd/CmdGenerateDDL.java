package cn.edu.ruc.iir.rainbow.layout.cmd;

import cn.edu.ruc.iir.rainbow.common.cmd.Command;
import cn.edu.ruc.iir.rainbow.common.cmd.Receiver;
import cn.edu.ruc.iir.rainbow.common.exception.ExceptionHandler;
import cn.edu.ruc.iir.rainbow.common.exception.ExceptionType;
import cn.edu.ruc.iir.rainbow.layout.domian.FileFormat;
import cn.edu.ruc.iir.rainbow.layout.sql.GenerateDDL;

import java.io.IOException;
import java.util.Properties;

public class CmdGenerateDDL implements Command
{
    private Receiver receiver = null;

    @Override
    public void setReceiver(Receiver receiver)
    {
        this.receiver = receiver;
    }

    /**
     * params should contain the following settings:
     * <ol>
     *   <li>file.format, orc, parquet, text</li>
     *   <li>table.name</li>
     *   <li>schema.file</li>
     *   <li>ddl.file</li>
     * </ol>
     *
     * this method will pass the following results to receiver:
     * <ol>
     *   <li>ddl.file</li>
     * </ol>
     * @param params
     */
    @Override
    public void execute(Properties params)
    {
        FileFormat format = FileFormat.valueOf(params.getProperty("file.format"));
        String schemaFilePath = params.getProperty("schema.file");
        String ddlFilePath = params.getProperty("ddl.file");
        String tableName = params.getProperty("table.name");
        Properties results = new Properties();
        try
        {
            switch (format)
            {
                case ORC:
                    GenerateDDL.GenCreateOrc(tableName, schemaFilePath, ddlFilePath);
                    results.setProperty("ddl.file", ddlFilePath);
                    break;
                case PARQUET:
                    GenerateDDL.GenCreateParq(tableName, schemaFilePath, ddlFilePath);
                    results.setProperty("ddl.file", ddlFilePath);
                    break;
                case TEXT:
                    GenerateDDL.GenCreateText(schemaFilePath, ddlFilePath);
                    results.setProperty("ddl.file", ddlFilePath);
                    break;
            }
        } catch (IOException e)
        {
            ExceptionHandler.Instance().log(ExceptionType.ERROR, "I/O error, check the file paths", e);
        }

        if (this.receiver == null)
        {
            receiver.action(results);
        }
    }
}
