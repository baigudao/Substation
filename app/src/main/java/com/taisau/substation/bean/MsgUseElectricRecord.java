package com.taisau.substation.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 用电记录
 * Created by Devin on 2018/4/8 0008.
 */
@Entity
public class MsgUseElectricRecord {

//    public string Uid { get; set; }                 //唯一标识符
//    public string Serial { get; set; }              //设备序列号
//    public string CreateTime { get; set; }          //比对时间(格式"yyyy-MM-dd HH:mm:ss")
//    public string DScore { get; set; }              //比对分数
//    public EDetectResult DRes { get; set; }         //比对结果
//    public string TemplateImg { get; set; }         //模板照
//    public string SiteImg { get; set; }             //现场照
//    public int UpdateState { get; set; }            //更新状态  1实时数据 2历史数据
//    public int ElectricNumber { get; set; }         //某路电：1,2,3,4,5,6,7,8
//    public int UseElectricState { get; set; }       //用电状态，0：关电；1：取电
//    public int ExceptionInfo { get; set; }          //异常信息，0：正常；1：漏电；2：断电；3：其它


    @NotNull
    private String uid;//唯一标识符
    private String serial_num;//设备序列号
    private String create_time;//记录产生的时间(格式"yyyy-MM-dd HH:mm:ss")
    private String score_num;//比对分数
    private int detectInfo;//比对结果
    private String template_image;//模板照
    private String site_image;//现场照
    private int updateState;//更新状态  1实时数据 2历史数据
    private int electric_num; //某路电：1,2,3,4,5,6,7,8
    private int useElectricState;//用电状态，0：关电；1：取电
    private int exception_info;//异常信息，0：正常；1：漏电；2：断电；3：其它

    @Id
    private Long id;

    private boolean upload_status;//上传成功：true;上传失败：false


    @Generated(hash = 90983353)
    public MsgUseElectricRecord(@NotNull String uid, String serial_num, String create_time,
            String score_num, int detectInfo, String template_image, String site_image,
            int updateState, int electric_num, int useElectricState, int exception_info,
            Long id, boolean upload_status) {
        this.uid = uid;
        this.serial_num = serial_num;
        this.create_time = create_time;
        this.score_num = score_num;
        this.detectInfo = detectInfo;
        this.template_image = template_image;
        this.site_image = site_image;
        this.updateState = updateState;
        this.electric_num = electric_num;
        this.useElectricState = useElectricState;
        this.exception_info = exception_info;
        this.id = id;
        this.upload_status = upload_status;
    }


    @Generated(hash = 700232961)
    public MsgUseElectricRecord() {
    }


    public Long getId() {
        return this.id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getUid() {
        return this.uid;
    }


    public void setUid(String uid) {
        this.uid = uid;
    }


    public String getSerial_num() {
        return this.serial_num;
    }


    public void setSerial_num(String serial_num) {
        this.serial_num = serial_num;
    }


    public String getCreate_time() {
        return this.create_time;
    }


    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }


    public String getScore_num() {
        return this.score_num;
    }


    public void setScore_num(String score_num) {
        this.score_num = score_num;
    }


    public int getDetectInfo() {
        return this.detectInfo;
    }


    public void setDetectInfo(int detectInfo) {
        this.detectInfo = detectInfo;
    }


    public String getTemplate_image() {
        return this.template_image;
    }


    public void setTemplate_image(String template_image) {
        this.template_image = template_image;
    }


    public String getSite_image() {
        return this.site_image;
    }


    public void setSite_image(String site_image) {
        this.site_image = site_image;
    }


    public int getUpdateState() {
        return this.updateState;
    }


    public void setUpdateState(int updateState) {
        this.updateState = updateState;
    }


    public int getElectric_num() {
        return this.electric_num;
    }


    public void setElectric_num(int electric_num) {
        this.electric_num = electric_num;
    }


    public int getUseElectricState() {
        return this.useElectricState;
    }


    public void setUseElectricState(int useElectricState) {
        this.useElectricState = useElectricState;
    }


    public int getException_info() {
        return this.exception_info;
    }


    public void setException_info(int exception_info) {
        this.exception_info = exception_info;
    }


    public boolean getUpload_status() {
        return this.upload_status;
    }


    public void setUpload_status(boolean upload_status) {
        this.upload_status = upload_status;
    }

    
}
