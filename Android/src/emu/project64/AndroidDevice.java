/****************************************************************************
*                                                                           *
* Project 64 - A Nintendo 64 emulator.                                      *
* http://www.pj64-emu.com/                                                  *
* Copyright (C) 2012 Project64. All rights reserved.                        *
*                                                                           *
* License:                                                                  *
* GNU/GPLv2 http://www.gnu.org/licenses/gpl-2.0.html                        *
*                                                                           *
****************************************************************************/
package emu.project64;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import emu.project64.util.Strings;
import emu.project64.util.FileUtil;
import emu.project64.util.Utility;
import tv.ouya.console.api.OuyaFacade;
import android.os.Build;
import android.os.Environment;

public class AndroidDevice
{
	/** True if device is running Gingerbread or later (9 - Android 2.3.x) */
	public static final boolean IS_GINGERBREAD = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	
	/** True if device is running Honeycomb or later (11 - Android 3.0.x) */
	public static final boolean IS_HONEYCOMB = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	
	/** True if device is running Honeycomb MR1 or later (12 - Android 3.1.x) */
	public static final boolean IS_HONEYCOMB_MR1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
	
	/** True if device is running Ice Cream Sandwich or later (14 - Android 4.0.x) */
	public static final boolean IS_ICE_CREAM_SANDWICH = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	
	/** True if device is running Jellybean or later (16 - Android 4.1.x) */
	public static final boolean IS_JELLY_BEAN = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	
	/** True if device is running KitKat or later (19 - Android 4.4.x) */
	public static final boolean IS_KITKAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	
	/** True if device is an OUYA. */
	public static final boolean IS_OUYA_HARDWARE = OuyaFacade.getInstance().isRunningOnOUYAHardware();
	
	public final static String EXTERNAL_PUBLIC_DIRECTORY = Environment.getExternalStorageDirectory().getPath();
	public final static String PACKAGE_DIRECTORY = EXTERNAL_PUBLIC_DIRECTORY + "/Android/data/" + AndroidDevice.class.getPackage().getName();

	public static final boolean IS_ACTION_BAR_AVAILABLE = AndroidDevice.IS_HONEYCOMB && !AndroidDevice.IS_OUYA_HARDWARE;

	final static boolean isTv;
    static 
    {
		isTv = Project64Application.getAppContext().getPackageManager().hasSystemFeature("android.software.leanback");
    }
    
    public static boolean isAndroidTv() 
    {
        return isTv;
    }
    
    public static ArrayList<String> getStorageDirectories() 
    {
        BufferedReader bufReader = null;
        ArrayList<String> list = new ArrayList<String>();
        list.add(EXTERNAL_PUBLIC_DIRECTORY);

        List<String> typeWL = Arrays.asList("vfat", "exfat", "sdcardfs", "fuse", "ntfs", "fat32", "ext3", "ext4", "esdfs");
        List<String> typeBL = Arrays.asList("tmpfs");
        String[] mountWL = {"/mnt", "/Removable", "/storage"};
        String[] mountBL = 
    	{
            "/mnt/secure",
            "/mnt/shell",
            "/mnt/asec",
            "/mnt/obb",
            "/mnt/media_rw/extSdCard",
            "/mnt/media_rw/sdcard",
            "/storage/emulated"
        };
        String[] deviceWL = 
    	{
	        "/dev/block/vold",
	        "/dev/fuse",
	        "/mnt/media_rw"
        };

        try 
        {
            bufReader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            while ((line = bufReader.readLine()) != null) 
            {
                StringTokenizer tokens = new StringTokenizer(line, " ");
                String device = tokens.nextToken();
                String mountpoint = tokens.nextToken();
                String type = tokens.nextToken();

                // skip if already in list or if type/mountpoint is blacklisted
                if (list.contains(mountpoint) || typeBL.contains(type) || Strings.startsWith(mountBL, mountpoint))
                {
                    continue;
                }

                // check that device is in whitelist, and either type or mountpoint is in a whitelist
                if (Strings.startsWith(deviceWL, device) && (typeWL.contains(type) || Strings.startsWith(mountWL, mountpoint))) 
                {
                    int position = Strings.containsName(list, FileUtil.getFileNameFromPath(mountpoint));
                    if (position > -1)
                    {
                        list.remove(position);
                    }
                    list.add(mountpoint);
                }
            }
        }
        catch (FileNotFoundException e) 
        {
        }
        catch (IOException e) 
        {
        }
        finally 
        {
        	Utility.close(bufReader);
        }
        return list;
    }
}
