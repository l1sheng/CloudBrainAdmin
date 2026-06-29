package com.neuCloudBrainMedical.admin.service.doctor;

import com.neuCloudBrainMedical.admin.dto.doctor.BatchImportResult;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorCreateRequest;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorDisableCheckResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorRoleOption;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorUpdateRequest;

import java.util.List;

/**
 * 医生命令服务（写操作）。
 * 与 Query 服务分离，遵循 Interface Segregation。
 */
public interface IDoctorCommandService {

	/** 获取所有可用的医生权限角色（供前端下拉选择）。 */
	List<DoctorRoleOption> listDoctorRoles();

	DoctorResponse createDoctor(DoctorCreateRequest request);

	DoctorResponse updateDoctor(Long id, DoctorUpdateRequest request);

	/**
	 * 切换医生启用/禁用状态。
	 * 若 force=true 时强制禁用（即使有未完成的排班/挂号）。
	 */
	DoctorResponse toggleStatus(Long id, boolean force);

	/**
	 * 检查禁用前是否存在未完成的排班/挂号。
	 */
	DoctorDisableCheckResponse checkBeforeDisable(Long id);

	BatchImportResult batchImport(List<DoctorCreateRequest> list);

	void deleteDoctor(Long id);
}