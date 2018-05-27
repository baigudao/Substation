package bid.vcoo.plat;


import android.os.Build;
import android.util.Log;

/**
 * Created by carter on 2016/12/6.
 */

public class I2Cdm {

    private static final String TAG = "I2Cdm";
    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */

    public static boolean set_macaddr(byte[] mac) {
        String model = Build.MODEL;
        String devpath = "/dev/i2c-2";
        if (model.equalsIgnoreCase("a20c")) {
            devpath = "/dev/i2c-1";
        } else if (model.equalsIgnoreCase("rk3188")) {
            //Log.d(TAG,"rk3188 model");
            devpath = "/dev/i2c-2";
        } else if (model.equalsIgnoreCase("rk3288")) {
            //Log.d(TAG,"rk3188 model");
            devpath = "/dev/i2c-0";
        }

        int ret = save_macaddr(mac, devpath);
        if (ret != -1) {
            //reboot
            return true;
        }
        return false;
    }

    public static boolean check_dm_license(byte[] keys) {
        String model = Build.MODEL;
        String devpath = "/dev/i2c-0";
        if (model.equalsIgnoreCase("a20c")) {
            devpath = "/dev/i2c-1";
        } else if (model.equalsIgnoreCase("rk3188")) {
            devpath = "/dev/i2c-2";
        } else if (model.equalsIgnoreCase("rk3288")) {
            devpath = "/dev/i2c-0";
        }

        int ret = checklicense(devpath);
        if (ret != -1) {
            //check ok
            Log.e("i2cdm", "" + ret);
            return true;
        }
        //no license
        return false;
    }

    public static boolean set_cfg_file_eeprom(String filepath) {
        String model = Build.MODEL;
        String devpath = "/sys/bus/i2c/drivers/eeprom/syscfg";
        int ret = save_panelcfg_eeprom(filepath, devpath);
        if (ret != -1) {
            //reboot
            return true;
        }
        return false;
    }

    public static boolean set_cfg_file(String filepath) {
        String devpath2 = "/dev/i2c-2";
        String devpath1 = "/dev/i2c-1";

        int ret = save_panelcfg(filepath, devpath2);
        if (ret > 0) {
            //reboot
            return true;
        } else {
            ret = save_panelcfg(filepath, devpath1);
            if (ret > 0)
                //reboot
                return true;
        }

        return false;
    }

    private static native int save_panelcfg(String filepath, String devpath);

    private static native int save_panelcfg_eeprom(String filepath, String devpath);

    private static native int save_macaddr(byte[] mac, String devpath);

    private static native int checklicense(String devpath);


    static {
        System.loadLibrary("I2Cdm");
    }
}

