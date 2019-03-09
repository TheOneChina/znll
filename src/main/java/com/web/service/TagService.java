package com.web.service;

import com.tnsoft.hibernate.model.Express;
import com.tnsoft.hibernate.model.Tag;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.web.model.Response;

import java.util.List;

public interface TagService extends BaseService<Tag> {

    /**
     * 将设备添加至账户
     *
     * @param tagNo    设备识别码
     * @param domainId 机构ID
     * @return response
     */
    Response scanTag(String tagNo, int domainId);

    //医药版启用设备
    Response enableTag(String tagNo, int userId, Integer flag);

    /**
     * 根据设备识别码获取当前使用该设备的用户
     *
     * @param tagNo 设备识别码
     * @return 当前使用该设备的用户
     */
    User getCurrentUserByTagNo(String tagNo);

    List<Express> getAllExpresses(String tagNo);

    Response saveTagAPConfig(String SSID, String password, String tagNo);

    List<Tag> getTagByUId(Integer userId);

    Tag getTagByEId(int expressId);

    Response editBuzzer(String tagNo, int model);

    Response editTag(String[] tagNo, String SSID, String password, String lowTemp, String highTemp, String
            uploadCycle, Integer buzzer, Integer appointStart, String alertPhones, int flag);

    Response tagTemplate(String[] tagNos, Integer model);

    Response editAppointStart(String tagNo, String time);

    Response editName(String tagNo, String name);

    Response editAlertPhones(String tagNo, String alertPhones);

    Response editSleepTime(String tagNo, Integer minutes);

    Response editTemperature(String tagNo, Float min, Float max);

    Tag getTagInfoByNoAndUserId(String tagNo, int userId);

    Response deleteTag(String tagNo, int userId);

    boolean createNewTags(int nums, int hardwareType);

    boolean saveCalibration(String tagNo, int calibrationType, float standardLowTemp, float lowTemp, float
            standardMediumTemp, float mediumTemp, float
                                    standardHighTemp, float highTemp, float standardHumidity, float humidity);

    Response addSMS(String[] tagNos, int num);

    /**
     * 增加设备的平台使用时间
     *
     * @param tagNos 设备识别号数组
     * @param year   增加年数
     * @return Response
     */
    Response addServiceTime(String[] tagNos, int year);

}
