package com.shuttersky.liarsdice;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class LogFileFilter extends FileFilter
{
    public boolean accept(File f)
    {
        if (f != null)
        {
            if (f.isDirectory())
            {
                return true;
            }
            String extension = getExtension(f);
            if (GameServer.GAME_LOG_EXT.equals(extension))
            {
                return true;
            }
        }
        return false;
    }

    public String getDescription()
    {
        return "Log Files";
    }

    private String getExtension(File f)
    {
        String ext = null;
        if (f != null)
        {
            final var filename = f.getName();
            final var i = filename.lastIndexOf('.');
            if (i > 0 && i < filename.length() - 1)
            {
                ext = filename.substring(i + 1).toLowerCase();
            }
        }
        return ext;
    }
}