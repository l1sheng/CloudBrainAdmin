package com.neuCloudBrainMedical.admin.dto.doctor;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量导入结果。
 */
public class BatchImportResult {

	private int total;
	private int success;
	private int failed;
	private List<ImportError> errors;

	public BatchImportResult() {
		this.errors = new ArrayList<>();
	}

	public void recordSuccess() {
		total++;
		success++;
	}

	public void recordError(int index, String message) {
		total++;
		failed++;
		errors.add(new ImportError(index, message));
	}

	public int getTotal() { return total; }
	public void setTotal(int total) { this.total = total; }

	public int getSuccess() { return success; }
	public void setSuccess(int success) { this.success = success; }

	public int getFailed() { return failed; }
	public void setFailed(int failed) { this.failed = failed; }

	public List<ImportError> getErrors() { return errors; }
	public void setErrors(List<ImportError> errors) { this.errors = errors; }

	public static class ImportError {
		private int row;
		private String message;

		public ImportError() {}

		public ImportError(int row, String message) {
			this.row = row;
			this.message = message;
		}

		public int getRow() { return row; }
		public void setRow(int row) { this.row = row; }

		public String getMessage() { return message; }
		public void setMessage(String message) { this.message = message; }
	}
}



