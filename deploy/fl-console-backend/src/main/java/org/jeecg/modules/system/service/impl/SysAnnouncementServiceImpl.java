package org.jeecg.modules.system.service.impl;

import java.util.*;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.dto.message.BusTemplateMessageDTO;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.SysAnnmentTypeEnum;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.message.entity.SysMessageTemplate;
import org.jeecg.modules.message.service.ISysMessageTemplateService;
import org.jeecg.modules.system.entity.SysAnnouncement;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysAnnouncementMapper;
import org.jeecg.modules.system.mapper.SysAnnouncementSendMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.service.ISysAnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 系统通告表
 * @Author: jeecg-boot
 * @Date:  2019-01-02
 * @Version: V1.0
 */
@Service
public class SysAnnouncementServiceImpl extends ServiceImpl<SysAnnouncementMapper, SysAnnouncement> implements ISysAnnouncementService {

	@Resource
	private SysAnnouncementMapper sysAnnouncementMapper;

	@Resource
	private SysAnnouncementSendMapper sysAnnouncementSendMapper;
	@Resource
	private SysUserMapper userMapper;

	@Autowired
	ISysMessageTemplateService sysMessageTemplateService;

	@Transactional
	@Override
	public void saveAnnouncement(SysAnnouncement sysAnnouncement) {
		if(sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_ALL)) {
			sysAnnouncementMapper.insert(sysAnnouncement);
		}else {
			// 1.插入通告表记录
			sysAnnouncementMapper.insert(sysAnnouncement);
			// 2.插入用户通告阅读标记表记录
			String userId = sysAnnouncement.getUserIds();
			String[] userIds = userId.substring(0, (userId.length()-1)).split(",");
			String anntId = sysAnnouncement.getId();
			Date refDate = new Date();
			for(int i=0;i<userIds.length;i++) {
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(anntId);
				announcementSend.setUserId(userIds[i]);
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				announcementSend.setReadTime(refDate);
				sysAnnouncementSendMapper.insert(announcementSend);
			}
		}
	}

	/**
	 * @功能：编辑消息信息
	 */
	@Transactional
	@Override
	public boolean upDateAnnouncement(SysAnnouncement sysAnnouncement) {
		// 1.更新系统信息表数据
		sysAnnouncementMapper.updateById(sysAnnouncement);
		String userId = sysAnnouncement.getUserIds();
		if(oConvertUtils.isNotEmpty(userId)&&sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_UESR)) {
			// 2.补充新的通知用户数据
			String[] userIds = userId.substring(0, (userId.length()-1)).split(",");
			String anntId = sysAnnouncement.getId();
			Date refDate = new Date();
			for(int i=0;i<userIds.length;i++) {
				LambdaQueryWrapper<SysAnnouncementSend> queryWrapper = new LambdaQueryWrapper<SysAnnouncementSend>();
				queryWrapper.eq(SysAnnouncementSend::getAnntId, anntId);
				queryWrapper.eq(SysAnnouncementSend::getUserId, userIds[i]);
				List<SysAnnouncementSend> announcementSends=sysAnnouncementSendMapper.selectList(queryWrapper);
				if(announcementSends.size()<=0) {
					SysAnnouncementSend announcementSend = new SysAnnouncementSend();
					announcementSend.setAnntId(anntId);
					announcementSend.setUserId(userIds[i]);
					announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
					announcementSend.setReadTime(refDate);
					sysAnnouncementSendMapper.insert(announcementSend);
				}
			}
			// 3. 删除多余通知用户数据
			Collection<String> delUserIds = Arrays.asList(userIds);
			LambdaQueryWrapper<SysAnnouncementSend> queryWrapper = new LambdaQueryWrapper<SysAnnouncementSend>();
			queryWrapper.notIn(SysAnnouncementSend::getUserId, delUserIds);
			queryWrapper.eq(SysAnnouncementSend::getAnntId, anntId);
			sysAnnouncementSendMapper.delete(queryWrapper);
		}
		return true;
	}

	// @功能：流程执行完成保存消息通知
	@Override
	public void saveSysAnnouncement(String title, String msgContent) {
		SysAnnouncement announcement = new SysAnnouncement();
		announcement.setTitile(title);
		announcement.setMsgContent(msgContent);
		announcement.setSender("JEECG BOOT");
		announcement.setPriority(CommonConstant.PRIORITY_L);
		announcement.setMsgType(CommonConstant.MSG_TYPE_ALL);
		announcement.setSendStatus(CommonConstant.HAS_SEND);
		announcement.setSendTime(new Date());
		announcement.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
		sysAnnouncementMapper.insert(announcement);
	}

	@Override
	public Page<SysAnnouncement> querySysCementPageByUserId(Page<SysAnnouncement> page, String userId,String msgCategory) {
		 return page.setRecords(sysAnnouncementMapper.querySysCementListByUserId(page, userId, msgCategory));
	}

	@Override
	public Page<SysAnnouncement> querySysCementPageByUserIdAndPartyId(Page<SysAnnouncement> page, String userId, int partyId, String msgCategory,String readStatus) {
		return page.setRecords(sysAnnouncementMapper.querySysCementListByUserIdAndPartyId(page, userId, partyId, msgCategory, readStatus));
	}

	@Override
	public void sendJobAnnouncement(JSONObject message) {
		String templateCode = message.getString("templateCode");
		String title = message.getString("title");
		String fromUserId = message.getString("fromUserId");
		String fromUserName = message.getString("fromUserName");
		int fromPartyId = message.getIntValue("fromPartyId");
		String toUserId = message.getString("toUserId");
		int toPartyId = message.getIntValue("toPartyId");
		String busId = message.getString("busId");
		String busType = message.getString("busType");
		//2消息通知，3申请授权
		String msgCategory = message.getString("msgCategory");

		List<SysMessageTemplate> sysSmsTemplates = sysMessageTemplateService.selectByCode(templateCode);
		if(sysSmsTemplates==null||sysSmsTemplates.size()==0){
			throw new JeecgBootException("消息模板不存在，模板编码："+templateCode);
		}
		SysMessageTemplate sysSmsTemplate = sysSmsTemplates.get(0);
		//模板标题
		title = title==null?sysSmsTemplate.getTemplateName():title;
		//模板内容
		String content = sysSmsTemplate.getTemplateContent();

		for (Map.Entry<String, Object> entry : message.entrySet()) {
			String str = "${" + entry.getKey() + "}";
			title = title.replace(str, entry.getValue().toString());
			content = content.replace(str, entry.getValue().toString());
		}

		SysAnnouncement announcement = new SysAnnouncement();
		announcement.setTitile(title);
		announcement.setMsgContent(content);
		announcement.setSender(fromUserName);
		announcement.setPriority(CommonConstant.PRIORITY_M);
		announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
		announcement.setSendStatus(CommonConstant.HAS_SEND);
		announcement.setSendTime(new Date());
		announcement.setMsgCategory(msgCategory);
		announcement.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
		announcement.setBusId(busId);
		announcement.setBusType(busType);

//		if(busType!=null){
//			announcement.setOpenType(SysAnnmentTypeEnum.getByType(busType).getOpenType());
//			announcement.setOpenPage(SysAnnmentTypeEnum.getByType(busType).getOpenPage());
//		}

		sysAnnouncementMapper.insert(announcement);
		// 2.插入用户通告阅读标记表记录
		String anntId = announcement.getId();

		SysAnnouncementSend announcementSend = new SysAnnouncementSend();
		announcementSend.setAnntId(anntId);
		announcementSend.setUserId(toUserId);
		announcementSend.setPartyId(toPartyId);
		announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
		sysAnnouncementSendMapper.insert(announcementSend);

	}
}
