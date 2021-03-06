package com.example.taupstairs.bean;

public class InfoExecTask {
	
	private String signUpStringReply;
	private String statusId;
	private String statusPersonId;
	private String statusPersonNickname;
	private String statusTitle;
	private String signUpStringNickname;
	private String signUpString;
	private String personContact;
	private String personPhone;
	private String personQq;
	private String personEmail;
	
	public String getSignUpStringReply() {
		return signUpStringReply;
	}
	public void setSignUpStringReply(String signUpStringReply) {
		this.signUpStringReply = signUpStringReply;
	}
	public String getStatusId() {
		return statusId;
	}
	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}
	public String getStatusPersonId() {
		return statusPersonId;
	}
	public void setStatusPersonId(String statusPersonId) {
		this.statusPersonId = statusPersonId;
	}
	public String getStatusPersonNickname() {
		return statusPersonNickname;
	}
	public void setStatusPersonNickname(String statusPersonNickname) {
		this.statusPersonNickname = statusPersonNickname;
	}
	public String getStatusTitle() {
		return statusTitle;
	}
	public void setStatusTitle(String statusTitle) {
		this.statusTitle = statusTitle;
	}
	public String getSignUpStringNickname() {
		return signUpStringNickname;
	}
	public void setSignUpStringNickname(String signUpStringNickname) {
		this.signUpStringNickname = signUpStringNickname;
	}
	public String getSignUpString() {
		return signUpString;
	}
	public void setSignUpString(String signUpString) {
		this.signUpString = signUpString;
	}
	public String getPersonContact() {
		return personContact;
	}
	public void setPersonContact(String personContact) {
		this.personContact = personContact;
	}
	public String getPersonPhone() {
		return personPhone;
	}
	public void setPersonPhone(String personPhone) {
		this.personPhone = personPhone;
	}
	public String getPersonQq() {
		return personQq;
	}
	public void setPersonQq(String personQq) {
		this.personQq = personQq;
	}
	public String getPersonEmail() {
		return personEmail;
	}
	public void setPersonEmail(String personEmail) {
		this.personEmail = personEmail;
	}
	@Override
	public String toString() {
		return "InfoExecTask [signUpStringReply=" + signUpStringReply
				+ ", statusId=" + statusId + ", statusPersonId="
				+ statusPersonId + ", statusPersonNickname="
				+ statusPersonNickname + ", statusTitle=" + statusTitle
				+ ", signUpStringNickname=" + signUpStringNickname
				+ ", signUpString=" + signUpString + ", personContact="
				+ personContact + ", personPhone=" + personPhone
				+ ", personQq=" + personQq + ", personEmail=" + personEmail
				+ "]";
	}
	
}
