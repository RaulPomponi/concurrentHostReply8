package it.xview.cp.util;

public class ConcurrentParameter {
	public String getEndUserColumnName() {
		return endUserColumnName;
	}
	public void setEndUserColumnName(String endUserColumnName) {
		this.endUserColumnName = endUserColumnName;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public boolean isRequiredFlag() {
		return requiredFlag;
	}
	public void setRequiredFlag(boolean requiredFlag) {
		this.requiredFlag = requiredFlag;
	}
	public boolean isDisplayFlag() {
		return displayFlag;
	}
	public void setDisplayFlag(boolean displayFlag) {
		this.displayFlag = displayFlag;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public int getColumnSeqNum() {
		return columnSeqNum;
	}
	public void setColumnSeqNum(int columnSeqNum) {
		this.columnSeqNum = columnSeqNum;
	}
	public int getArgument() {
		return argument;
	}
	public void setArgument(int argument) {
		this.argument = argument;
	}
	private String endUserColumnName;
	private String dataType;
	private  boolean requiredFlag;
	private  boolean displayFlag;
	private String defaultValue;
	private int columnSeqNum;
	private int argument;

}
