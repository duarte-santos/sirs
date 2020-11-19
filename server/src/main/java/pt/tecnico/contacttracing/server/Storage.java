
/* ====================================================================== */
/* ====[                   SIRS - Contact Tracing                   ]==== */
/* ====[                   Server - Basic Version                   ]==== */
/* ====================================================================== */

// status - receive a single pair of <number, key>, not encrypted

package pt.tecnico.contacttracing.server;

import java.net.*;
import com.google.gson.*;
import java.util.List;
import java.util.ArrayList;
import java.time.Instant;


public class Storage {
	// Class InfectedData
	public class InfectedData {
		private int _numbers;
		private int _keys;
		private Instant _date;

		InfectedData(int numbers, int keys, Instant date) {
			_numbers = numbers;
			_keys = keys;
			_date = date;
		}

		public int getNumbers(){
			return _numbers;
		}

		public int getKeys(){
			return _keys;
		}

		public Instant getTimestamp(){
			return _date;
		}
    }
    
    private List<InfectedData> _storage = new ArrayList<InfectedData>();

	public List<InfectedData> getInfectedData() {
		return _storage;
	}
	public void addInfectedData(InfectedData data) {
		_storage.add(data);
	}

	public void storeInfectedData(int number, int key, Instant timestamp) {
		InfectedData data = new InfectedData(number, key, timestamp);
		addInfectedData(data);
	}

	public List<InfectedData> getUpdates(Instant lastUpdate) {
		List<InfectedData> all_data = getInfectedData();
		List<InfectedData> new_data = new ArrayList<InfectedData>();

		for (InfectedData data : all_data)
			if (data.getTimestamp().compareTo(lastUpdate) >= 0) new_data.add(data);

		return new_data;
	}

}
