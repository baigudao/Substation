package com.taisau.substation.ui.setting.model;

import com.taisau.substation.SubstationApplication;
import com.taisau.substation.ui.setting.presenter.SettingPresenter;
import com.taisau.substation.util.FileUtils;
import com.taisau.substation.util.Preference;
import com.taisau.substation.util.ThreadPoolUtils;

import java.io.File;

import static com.taisau.substation.util.Constant.LIB_DIR;

/**
 * Created by whx on 2017-08-15
 */

public class SettingModel implements ISettingModel {

    File file[] = new File[3];

    private SettingPresenter presenter;

    public SettingModel(SettingPresenter settingPresenter) {
        this.presenter = settingPresenter;
    }

    @Override
    public void clearDevice() {
        ThreadPoolUtils.execute(() -> {
            file[0] = new File(LIB_DIR + "/card_fea");
//        file[1] = new File(LIB_DIR + "/face_fea");
            file[1] = new File(LIB_DIR + "/face_img");
            file[2] = new File(LIB_DIR + "/card_img");
            Preference.clearUser();
//        deleteFile(whiteFile);
            deleteFile(file);
            SubstationApplication.getApplication().getDaoSession().getHistoryDao().deleteAll();
            SubstationApplication.getApplication().getDaoSession().getPersonDao().deleteAll();
            presenter.clearDataComplete();
        });
    }

    private void deleteFile(File[] fileList) {
        int totalLength = 0;
        for (File file : fileList) {
            totalLength += file.list().length;
        }
        String[] fileNames = new String[totalLength];
        int currentLength = 0;
        for (File file : fileList) {
            String[] temp = file.list();
            for (int i = 0; i < temp.length; i++) {
                temp[i] = file.getAbsolutePath() + "/" + temp[i];
            }
            System.arraycopy(temp, 0, fileNames, currentLength, file.list().length);
            currentLength += file.list().length;
            FileUtils.deleteDirectory(file.getAbsolutePath());
            file.mkdir();
        }
        FileUtils.updateFileToSystem(SubstationApplication.getApplication(), fileNames, null);
    }
}
