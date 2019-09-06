package hermann.ebbinghaus;

public class TorchData {
	public String create_time;
	public int create_date;
	public int mem_date;
	public String mem_text;
	public int mem_status;
	public String mem_desc;

	public TorchData(String create_time, int create_date, int mem_date, String mem_text, int mem_status, String mem_desc) {
		this.create_time = create_time;
		this.create_date = create_date;
		this.mem_date = mem_date;
		this.mem_text = mem_text;
		this.mem_status = mem_status;
		this.mem_desc = mem_desc;
	}

}