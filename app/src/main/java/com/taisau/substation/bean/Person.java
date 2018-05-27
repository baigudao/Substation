package com.taisau.substation.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by whx on 2017/11/03
 */
@Entity
public class Person {
    @Unique
    @Id
    private Long id;
    @NotNull
    private String uid;//唯一标识符
    @NotNull
    private String ic_card;//ic卡号码
    private String img_path;//注册照片的存放路径
    private String fea_path;//注册照片提取的特征值的存放路径

    private String id_card;//身份证号码
    private int person_type;//白名单：0；黑名单：1;陌生人：2
    private float highest_score;//历史比对最高分，当产生更高的分数，要替换图片
    private String name;//姓名
    private int sex;//性别 0女，1男
    private int chang_index;//本次更改的图片的标识
    private String push_img_path1;//服务器下发的图片
    private String push_img_path2;
    private String push_img_path3;
    private String push_fea_path1;//服务器下发图片的特征值
    private String push_fea_path2;
    private String push_fea_path3;
    private String phone;  //电话号码
    private String EnterDate;          //受雇日期
    private String LeaveDate;          //离职日期
    private String SiteBeginDate;       //地点开始日期
    private String SiteEndDate;         //地点结束日期
    private String SafetyCardExpiryDate;//安全卡到期日期

    @Generated(hash = 41247407)
    public Person(Long id, @NotNull String uid, @NotNull String ic_card, String img_path, String fea_path,
            String id_card, int person_type, float highest_score, String name, int sex, int chang_index,
            String push_img_path1, String push_img_path2, String push_img_path3, String push_fea_path1,
            String push_fea_path2, String push_fea_path3, String phone, String EnterDate, String LeaveDate,
            String SiteBeginDate, String SiteEndDate, String SafetyCardExpiryDate) {
        this.id = id;
        this.uid = uid;
        this.ic_card = ic_card;
        this.img_path = img_path;
        this.fea_path = fea_path;
        this.id_card = id_card;
        this.person_type = person_type;
        this.highest_score = highest_score;
        this.name = name;
        this.sex = sex;
        this.chang_index = chang_index;
        this.push_img_path1 = push_img_path1;
        this.push_img_path2 = push_img_path2;
        this.push_img_path3 = push_img_path3;
        this.push_fea_path1 = push_fea_path1;
        this.push_fea_path2 = push_fea_path2;
        this.push_fea_path3 = push_fea_path3;
        this.phone = phone;
        this.EnterDate = EnterDate;
        this.LeaveDate = LeaveDate;
        this.SiteBeginDate = SiteBeginDate;
        this.SiteEndDate = SiteEndDate;
        this.SafetyCardExpiryDate = SafetyCardExpiryDate;
    }

    @Generated(hash = 1024547259)
    public Person() {
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIc_card() {
        return this.ic_card;
    }

    public void setIc_card(String ic_card) {
        this.ic_card = ic_card;
    }

    public String getImg_path() {
        return this.img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public String getFea_path() {
        return this.fea_path;
    }

    public void setFea_path(String fea_path) {
        this.fea_path = fea_path;
    }

    public String getId_card() {
        return this.id_card;
    }

    public void setId_card(String id_card) {
        this.id_card = id_card;
    }

    public int getPerson_type() {
        return this.person_type;
    }

    public void setPerson_type(int person_type) {
        this.person_type = person_type;
    }

    public float getHighest_score() {
        return this.highest_score;
    }

    public void setHighest_score(float highest_score) {
        this.highest_score = highest_score;
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


    public int getChang_index() {
        return this.chang_index;
    }

    public void setChang_index(int chang_index) {
        this.chang_index = chang_index;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getPush_img_path1() {
        return this.push_img_path1;
    }

    public void setPush_img_path1(String push_img_path1) {
        this.push_img_path1 = push_img_path1;
    }

    public String getPush_img_path2() {
        return this.push_img_path2;
    }

    public void setPush_img_path2(String push_img_path2) {
        this.push_img_path2 = push_img_path2;
    }

    public String getPush_img_path3() {
        return this.push_img_path3;
    }

    public void setPush_img_path3(String push_img_path3) {
        this.push_img_path3 = push_img_path3;
    }

    public String getPush_fea_path1() {
        return this.push_fea_path1;
    }

    public void setPush_fea_path1(String push_fea_path1) {
        this.push_fea_path1 = push_fea_path1;
    }

    public String getPush_fea_path2() {
        return this.push_fea_path2;
    }

    public void setPush_fea_path2(String push_fea_path2) {
        this.push_fea_path2 = push_fea_path2;
    }

    public String getPush_fea_path3() {
        return this.push_fea_path3;
    }

    public void setPush_fea_path3(String push_fea_path3) {
        this.push_fea_path3 = push_fea_path3;
    }

    public List<String> getAllFilePath() {
        List<String> paths = new ArrayList<>();
        paths.add(getImg_path());
        paths.add(getPush_img_path1());
        paths.add(getPush_img_path2());
        paths.add(getPush_img_path3());
        paths.add(getFea_path());
        paths.add(getPush_fea_path1());
        paths.add(getPush_fea_path2());
        paths.add(getPush_fea_path3());
        return paths;
    }

    public List<String> getAllImgPath() {
        List<String> paths = new ArrayList<>();
        paths.add(getImg_path());
        paths.add(getPush_img_path1());
        paths.add(getPush_img_path2());
        paths.add(getPush_img_path3());
        return paths;
    }

    public List<String> getAllFeaPath() {
        List<String> paths = new ArrayList<>();
        paths.add(getFea_path());
        paths.add(getPush_fea_path1());
        paths.add(getPush_fea_path2());
        paths.add(getPush_fea_path3());
        return paths;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", ic_card='" + ic_card + '\'' +
                ", img_path='" + img_path + '\'' +
                ", fea_path='" + fea_path + '\'' +
                ", id_card='" + id_card + '\'' +
                ", person_type=" + person_type +
                ", highest_score=" + highest_score +
                ", name='" + name + '\'' +
                ", sex=" + sex +
                ", chang_index=" + chang_index +
                ", push_img_path1='" + push_img_path1 + '\'' +
                ", push_img_path2='" + push_img_path2 + '\'' +
                ", push_img_path3='" + push_img_path3 + '\'' +
                ", push_fea_path1='" + push_fea_path1 + '\'' +
                ", push_fea_path2='" + push_fea_path2 + '\'' +
                ", push_fea_path3='" + push_fea_path3 + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLeaveDate() {
        return this.LeaveDate;
    }

    public void setLeaveDate(String LeaveDate) {
        this.LeaveDate = LeaveDate;
    }

    public String getSiteBeginDate() {
        return this.SiteBeginDate;
    }

    public void setSiteBeginDate(String SiteBeginDate) {
        this.SiteBeginDate = SiteBeginDate;
    }

    public String getSiteEndDate() {
        return this.SiteEndDate;
    }

    public void setSiteEndDate(String SiteEndDate) {
        this.SiteEndDate = SiteEndDate;
    }

    public String getSafetyCardExpiryDate() {
        return this.SafetyCardExpiryDate;
    }

    public void setSafetyCardExpiryDate(String SafetyCardExpiryDate) {
        this.SafetyCardExpiryDate = SafetyCardExpiryDate;
    }

    public String getEnterDate() {
        return this.EnterDate;
    }

    public void setEnterDate(String EnterDate) {
        this.EnterDate = EnterDate;
    }
}

