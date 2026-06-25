package com.neuCloudBrainMedical.admin.service;

import com.neuCloudBrainMedical.admin.dto.department.DepartmentDTO;
import com.neuCloudBrainMedical.admin.entity.department.Department;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.repository.department.DepartmentRepository;
import com.neuCloudBrainMedical.admin.service.department.DepartmentService;
import com.neuCloudBrainMedical.admin.service.department.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DepartmentServiceImplTest {

	@Test
	void listEnabledDepartmentsReturnsDtosOnlyForStatusOne() {
		DepartmentRepository repository = mock(DepartmentRepository.class);
		Department department = new Department();
		department.setDeptId(1L);
		department.setDeptCode("NEURO");
		department.setDeptName("神经内科");
		department.setDeptType("临床");
		department.setLocation("门诊楼2层");
		department.setDescription("神经系统疾病诊疗");
		department.setStatus(1);
		when(repository.findByStatus(1)).thenReturn(List.of(department));
		DepartmentService service = new DepartmentServiceImpl(repository);

		List<DepartmentDTO> result = service.listEnabledDepartments();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getDeptId()).isEqualTo(1L);
		assertThat(result.get(0).getDeptName()).isEqualTo("神经内科");
	}

	@Test
	void getDepartmentByIdThrowsBusinessExceptionWhenMissing() {
		DepartmentRepository repository = mock(DepartmentRepository.class);
		when(repository.findById(99L)).thenReturn(Optional.empty());
		DepartmentService service = new DepartmentServiceImpl(repository);

		assertThatThrownBy(() -> service.getDepartmentById(99L))
				.isInstanceOf(BusinessException.class)
				.hasMessage("科室不存在");
	}
}