package be.betty.gwtp.shared.dto;

import java.io.Serializable;

public class Room_dto implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String code;
	private int bddId;
	
	public Room_dto() {} // for serialization purpose

	public Room_dto(String code, int bddId) {
		super();
		this.code = code;
		this.bddId = bddId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getBddId() {
		return bddId;
	}

	public void setBddId(int bddId) {
		this.bddId = bddId;
	}
	
	
}
