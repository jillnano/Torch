package hermann.ebbinghaus;

public class BaseData {

	public static class TorchData {
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

	public static class TorchPeakData {
		public String filename;
		public String realname;
		public float encode_0;
		public float encode_1;
		public String filepath;

		public TorchPeakData(String filename, String realname, float encode_0, float encode_1) {
			this.filename = filename;
			this.realname = realname;
			this.encode_0 = encode_0;
			this.encode_1 = encode_1;
		}

		public String toString() {
			return this.filepath;
		}

	}
}