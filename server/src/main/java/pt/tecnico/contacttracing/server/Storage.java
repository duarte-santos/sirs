
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
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.time.Instant;


public class Storage {
	// Class InfectedData
	public class InfectedData {
		private int _numbers;
		private int _keys;

		InfectedData(int numbers, int keys) {
			_numbers = numbers;
			_keys = keys;
		}

		public int getNumbers(){
			return _numbers;
		}

		public int getKeys(){
			return _keys;
		}
    }

	private Map<Instant, InfectedData> _storage = new ConcurrentHashMap<Instant, InfectedData>();

	public Map<Instant, InfectedData> getInfectedData() {
		return _storage;
	}
	public void addInfectedData(Instant instant, InfectedData data) {
		_storage.put(instant, data);
	}

	public void storeInfectedData(int number, int key, Instant instant) {
		InfectedData data = new InfectedData(number, key);
		addInfectedData(instant, data);
	}

	public List<InfectedData> getUpdates(Instant lastUpdate) {
		Map<Instant, InfectedData> all_data = getInfectedData();
		Set<Instant> all_ts = all_data.keySet();

		List<InfectedData> new_data = new ArrayList<InfectedData>();

		for (Instant ts : all_ts)
			if (ts.compareTo(lastUpdate) >= 0) {
				InfectedData relevant_data = all_data.get(ts);
				new_data.add(relevant_data);
			}

		return new_data;
	}

}
