package com.neuCloudBrainMedical.admin.dto.doctor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 新增医生请求。
 * 工号（doctorNo）必须全局唯一。
 */
public class DoctorCreateRequest {

	@NotBlank(message = "医生姓名不能为空")
	@Size(max = 50, message = "医生姓名长度不能超过50")
	private String name;

	@NotBlank(message = "工号不能为空")
	@Size(max = 50, message = "工号长度不能超过50")
	private String doctorNo;

	@Pattern(regexp = "^(男|女|其他)?$", message = "性别仅允许：男/女/其他")
	private String gender;

	@Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
	private String phone;

	@Email(message = "邮箱格式不正确")
	@Size(max = 100, message = "邮箱长度不能超过100")
	private String email;

	@NotNull(message = "请选择所属科室")
	private Long departmentId;

	@NotBlank(message = "职称不能为空")
	@Size(max = 50, message = "职称长度不能超过50")
	private String title;

	@Size(max = 30, message = "医生类型长度不能超过30")
	private String doctorType = "主治";

	@Size(max = 255, message = "专长长度不能超过255")
	private String specialty;

	private LocalDate hireDate;

	@Size(max = 500, message = "简介长度不能超过500")
	private String introduction;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getDoctorNo() { return doctorNo; }
	public void setDoctorNo(String doctorNo) { this.doctorNo = doctorNo; }

	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = gender; }

	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public Long getDepartmentId() { return departmentId; }
	public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getDoctorType() { return doctorType; }
	public void setDoctorType(String doctorType) { this.doctorType = doctorType; }

	public String getSpecialty() { return specialty; }
	public void setSpecialty(String specialty) { this.specialty = specialty; }

	public LocalDate getHireDate() { return hireDate; }
	public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

	public String getIntroduction() { return introduction; }
	public void setIntroduction(String introduction) { this.introduction = introduction; }
}



