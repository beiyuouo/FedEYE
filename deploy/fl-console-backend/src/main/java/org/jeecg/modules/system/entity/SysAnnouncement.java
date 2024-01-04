package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: 系统通告表
 * @Author: jeecg-boot
 * @Date:  2019-01-02
 * @Version: V1.0
 */
@Data
@TableName("sys_announcement")
@ApiModel(value="sys_announcement对象", description="系统通告")

public class SysAnnouncement implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
    /**
     * 标题
     */
    @Excel(name = "标题", width = 15)
    @ApiModelProperty(value = "标题")
    private java.lang.String titile;
    /**
     * 内容
     */
    @Excel(name = "内容", width = 30)
    @ApiModelProperty(value = "内容")
    private java.lang.String msgContent;
    /**
     * 开始时间
     */
    @Excel(name = "开始时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "开始时间")
    private java.util.Date startTime;
    /**
     * 结束时间
     */
    @Excel(name = "结束时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "结束时间")
    private java.util.Date endTime;
    /**
     * 发布人
     */
    @Excel(name = "发布人", width = 15)
    @ApiModelProperty(value = "发布人")
    private java.lang.String sender;
    /**
     * 优先级（L低，M中，H高）
     */
    @Excel(name = "优先级", width = 15, dicCode = "priority")
    @Dict(dicCode = "priority")
    @ApiModelProperty(value = "优先级")
    private java.lang.String priority;

    /**
     * 消息类型1:通知公告2:系统消息3:审核通知
     */
    @Excel(name = "消息类型", width = 15, dicCode = "msg_category")
    @Dict(dicCode = "msg_category")
    @ApiModelProperty(value = "消息类型 1:通知公告2:系统消息3:审核通知")
    private java.lang.String msgCategory;
    /**
     * 通告对象类型（USER:指定用户，ALL:全体用户）
     */
    @Excel(name = "通告对象类型", width = 15, dicCode = "msg_type")
    @Dict(dicCode = "msg_type")
    @ApiModelProperty(value = "通告对象类型（USER:指定用户，ALL:全体用户）")
    private java.lang.String msgType;
    /**
     * 发布状态（0未发布，1已发布，2已撤销）
     */
    @Excel(name = "发布状态", width = 15, dicCode = "send_status")
    @Dict(dicCode = "send_status")
    @ApiModelProperty(value = "发布状态（0未发布，1已发布，2已撤销）")
    private java.lang.String sendStatus;
    /**
     * 发布时间
     */
    @Excel(name = "发布时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "发布时间")
    private java.util.Date sendTime;
    /**
     * 撤销时间
     */
    @Excel(name = "撤销时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "撤销时间")
    private java.util.Date cancelTime;
    /**
     * 删除状态（0，正常，1已删除）
     */
    @ApiModelProperty(value = "删除状态（0，正常，1已删除）")
    private java.lang.String delFlag;
    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private java.lang.String createBy;
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private java.util.Date createTime;
    /**
     * 更新人
     */
    @ApiModelProperty(value = "更新人")
    private java.lang.String updateBy;
    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private java.util.Date updateTime;
    /**
     * 指定用户
     **/
    @ApiModelProperty(value = "指定用户")
    private java.lang.String userIds;
    /**
     * 业务类型(email:邮件 bpm:流程)
     */
    @ApiModelProperty(value = "业务类型")
    private java.lang.String busType;
    /**
     * 业务id
     */
    @ApiModelProperty(value = "业务id")
    private java.lang.String busId;
    /**
     * 打开方式 组件：component 路由：url
     */
    @ApiModelProperty(value = "打开方式")
    private java.lang.String openType;
    /**
     * 组件/路由 地址
     */
    @ApiModelProperty(value = "组件/路由 地址")
    private java.lang.String openPage;
    /**
     * 摘要
     */
    @ApiModelProperty(value = "摘要")
    private java.lang.String msgAbstract;
}
