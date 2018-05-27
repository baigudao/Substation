package com.taisau.substation.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by whx on 2017/11/03
 */
@Entity
public class History {
    @Id
    private Long id;
    private String id_card;//身份证
    private String face_path;//现场人脸图片保存路径
    private String templatePhotoPath;//模版图片保存路径
    private String time; //比对时间
    private String com_status;//结果描述
    private float score;//分值
    private boolean upload_status;//上传成功：true;上传失败：false

    private int inOut;//出入口， 1:入，2，出
    private int result;//比对通过：0;比对失败：1,提示登记2，未知-1
    @NotNull
    private String ic_card;//IC卡
    private String name;
    private int sex;
    private int compareType;  //0：刷身份证，1：刷脸，2：刷IC卡

    @Generated(hash = 869423138)
    public History() {
    }


    @Generated(hash = 178005281)
    public History(Long id, String id_card, String face_path, String templatePhotoPath,
                   String time, String com_status, float score, boolean upload_status, int inOut,
                   int result, @NotNull String ic_card, String name, int sex, int compareType) {
        this.id = id;
        this.id_card = id_card;
        this.face_path = face_path;
        this.templatePhotoPath = templatePhotoPath;
        this.time = time;
        this.com_status = com_status;
        this.score = score;
        this.upload_status = upload_status;
        this.inOut = inOut;
        this.result = result;
        this.ic_card = ic_card;
        this.name = name;
        this.sex = sex;
        this.compareType = compareType;
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFace_path() {
        return this.face_path;
    }

    public void setFace_path(String face_path) {
        this.face_path = face_path;
    }


    public String getCom_status() {
        return this.com_status;
    }

    public void setCom_status(String com_status) {
        this.com_status = com_status;
    }

    public float getScore() {
        return this.score;
    }

    public void setScore(float score) {
        this.score = score;
    }


    public boolean getUpload_status() {
        return this.upload_status;
    }

    public void setUpload_status(boolean upload_status) {
        this.upload_status = upload_status;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSex() {
        return this.sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getId_card() {
        return this.id_card;
    }

    public void setId_card(String id_card) {
        this.id_card = id_card;
    }

    public String getTemplatePhotoPath() {
        return this.templatePhotoPath;
    }

    public void setTemplatePhotoPath(String templatePhotoPath) {
        this.templatePhotoPath = templatePhotoPath;
    }

    public int getInOut() {
        return this.inOut;
    }

    public void setInOut(int inOut) {
        this.inOut = inOut;
    }

    public String getIc_card() {
        return this.ic_card;
    }

    public void setIc_card(String ic_card) {
        this.ic_card = ic_card;
    }

    public int getCompareType() {
        return this.compareType;
    }

    public void setCompareType(int compareType) {
        this.compareType = compareType;
    }


    public int getResult() {
        return this.result;
    }


    public void setResult(int result) {
        this.result = result;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
