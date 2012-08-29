package be.betty.gwtp.shared.dto;

import java.io.Serializable;

public class ActivityState_dto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int day;
	private int period;

	private String room;
	
	public ActivityState_dto() {} //for serialisation
	
	public ActivityState_dto(int day, int period, String room) {
		super();
		this.day = day;
		this.period = period;
		this.room = room;
	}
	
	
	/**
	 * shouldn't be used.. (but the one with room argurment)
	 * 
	 * @param day
	 * @param period
	 */
	public ActivityState_dto(int day, int period) {
		super();
		this.day = day;
		this.period = period;
	}

	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}

	public int getRoom() {
		return Integer.parseInt(room);
	}

	
	
	

}
	