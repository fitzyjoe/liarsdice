/*
 * LogFileFilter.java
 *
 * Created on Aug 27, 2007, 11:26:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.shuttersky.liarsdice;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * @author joefitz
 */
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
            if (extension != null && GameServer.GAME_LOG_EXT.equals(extension))
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
        if (f != null)
        {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if (i > 0 && i < filename.length() - 1)
            {
                return filename.substring(i + 1).toLowerCase();
            }
            ;
        }
        return null;
    }


}
