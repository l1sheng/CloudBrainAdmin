package com.neuCloudBrainMedical.admin.service.doctor;

import com.neuCloudBrainMedical.admin.dto.doctor.DoctorOptionDTO;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorResponse;
import com.neuCloudBrainMedical.admin.dto.PageResponse;

import java.util.List;

/**
 * 医生查询服务（读操作）。
 * 遵循 Interface Segregation：与 Command 服务分离。
 */
public interface IDoctorQueryService {

	PageResponse<DoctorResponse> listDoctors(Long departmentId, String keyword,
	                                         Integer status, String title,
	                                         int pageNum, int pageSize);

	DoctorResponse getDoctorDetail(Long id);

	List<DoctorResponse> exportDoctors(Long departmentId);

	/** 排班管理页面的医生下拉列表：只返回启用状态。 */
	List<DoctorOptionDTO> listEnabledDoctors(Long departmentId);
}




