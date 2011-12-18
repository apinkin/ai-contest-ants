import java.util.ArrayList;
import java.util.TreeMap;
 
public class TopologicalSort {
	public static void main(String[] args) throws Exception {
		TreeMap<String, ArrayList<String>> mp = new TreeMap<String, ArrayList<String>>();
		String[] data, input = new String[] {
				"a: b c",
				"c: d"};
 
		for (String str : input)
			mp.put((data = str.split(":"))[0], Utils.aList(//
					data.length < 2 || data[1].trim().equals("")//
					? null : data[1].trim().split("\\s+")));
 
		Utils.tSortFix(mp);
		System.out.println(Utils.tSort(mp));
	}
}
