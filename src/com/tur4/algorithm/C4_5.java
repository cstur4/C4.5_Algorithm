\package com.tur4.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class C4_5 {

	private class Res{
		public boolean isPure = true;
		public String clazz;
	}
	
	private static Logger LOG = Logger.getLogger(C4_5.class);
	private static final String ROOT = "decision_tree";
	private static final String VALUE = "value";
	private static final String ALL = "all";
	private List<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
	private int decidx;
	private List<String> attrNames = new ArrayList<String>();
	private List<ArrayList<String>> attrValues = new ArrayList<ArrayList<String>>();
	private File file;
	private static String patternString = "@attribute\\s+([^\\s]+)\\s*\\{([^\\}]+)\\}";
	private Element root;
	private Document doc;
	private String outFilePath = "decisionTree.xml";
	private String mostClass;//未被覆盖的多数类
<<<<<<< HEAD
	private int[] flags;
=======
	
>>>>>>> 199a058cec47e7edd6e3238508d232ec1115ec5d
	public void setOutFilePath(String path){
		this.outFilePath = path;
	}
	
	public C4_5(String filePath, String decision, String split){
		this.file = new File(filePath);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(this.file));
			String str = null;
			boolean isData = false;
			while((str = reader.readLine()) != null){
				
				if(str.trim().length() == 0)
					continue;
				
				if(str.trim().startsWith("@data")){
					isData = true;
					continue;
				}
				
				if(str.trim().startsWith("@attribute")){
					Pattern pattern = Pattern.compile(patternString);
					Matcher m = pattern.matcher(str);
					if(m.find()){
						attrNames.add(m.group(1));
						ArrayList<String> values = new ArrayList<String>();
						String[] vals = m.group(2).split(split);
						for(String val: vals)
							values.add(val.trim());
						attrValues.add(values);
					}
				}else if(isData){
					String[] vals = str.split(split);
					ArrayList<String> record = new ArrayList<String>(attrNames.size());
					for(String val: vals)
						record.add(val.trim());
					data.add(record);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		//LOG.debug("attrNames=" + attrNames + "\r\nattrValues=" + attrValues + "\r\ndata=" + data);
		if(decision==null || decision.trim().length()==0)
			this.decidx = attrNames.size()-1;
		else
			setDecision(decision);
		
		flags = new int[data.size()];
		for(int i=0;i<data.size();++i)
			flags[i] = 0;
		doc = DocumentHelper.createDocument();
		root = doc.addElement(ROOT).addAttribute(VALUE, ALL);
	}
	
	public void setDecision(String decision){
		for(int i=0;i<attrNames.size();++i){
			if(attrNames.get(i).equals(decision)){
				decidx = i;
				break;
			}
		}
	}
	
	private double calcEntropy(int[] info){
		
		int sum = 0;
		for(int integer:info)
			sum += integer;
		if(sum == 0)
			return 0.0;
		double entropy = 0.0;
		
		for(int i=0;i<info.length;++i){
			entropy -= info[i] * Math.log(Double.MIN_VALUE + info[i]) / Math.log(2);
		}
		entropy += sum * Math.log(Double.MIN_VALUE + sum) / Math.log(2);
		//LOG.info("info=" + Arrays.toString(info)+"\tentropy="+entropy/sum);
		
		return entropy/sum;
	
	}
	
	private Integer findAttrValueIndex(int attrIdx, String val){
		
		List<String> attrs = attrValues.get(attrIdx);
		for(int i=0;i<attrs.size();++i)
			if(attrs.get(i).equals(val))
				return i;
		return null;
	}
	
	private double calcExpectEntropyByAttr(int attrIdx, List<Integer> idxSet){
		
		int diffValues = attrValues.get(attrIdx).size();
		int classNum = attrValues.get(decidx).size();
		int info[][] = new int[diffValues][];
		for(int i=0;i<diffValues;++i){
			info[i] = new int[classNum];
		}
		
		int count[] = new int[diffValues];
		
		for(Integer i: idxSet){
			List<String> record = data.get(i);
			String val = record.get(attrIdx);
			int idx = findAttrValueIndex(attrIdx, val);
			count[idx]++;
			
			String clazzVal = record.get(decidx);
			int classIdx = findAttrValueIndex(decidx, clazzVal);
			
			info[idx][classIdx]++;
		}
		double entropy = 0.0;
		double splitEntropy = 0.0;
		double sum = idxSet.size();
		
		for(int i=0;i<diffValues;++i){
			//LOG.debug("count[i]/sum * calcEntropy(info[i])="+(count[i]/sum)+"*"+calcEntropy(info[i]));
			entropy += count[i]/sum * calcEntropy(info[i]);
			splitEntropy -= count[i] * Math.log(Double.MIN_VALUE + count[i]) / Math.log(2);
		}
		
		splitEntropy += sum * Math.log(Double.MIN_VALUE + sum) / Math.log(2);
		splitEntropy /= sum;
		//LOG.debug("entropy="+entropy+"\tsplitEntropy="+splitEntropy+"\tres="+entropy/(splitEntropy + Double.MIN_VALUE));
		return entropy/(splitEntropy + Double.MIN_VALUE);
		
	}
	
	private Res pureInfo(List<Integer> set){
		
		Res res = new Res();
		String clazz = data.get(set.get(0)).get(decidx);
		for(Integer idx: set){
			if(!data.get(idx).get(decidx).equals(clazz)){
				res.isPure = false;
			}
		}
		res.clazz = clazz;
		return res;
		
	}
	
	public void buildTree(){
		List<Integer> records = new ArrayList<Integer>();
		for(int i=0;i<data.size();++i)
			records.add(i);
		
		ArrayList<String> attrs = new ArrayList<String>();
		for(String attr: attrNames){
			if(!attr.equals(attrNames.get(decidx)))
				attrs.add(attr);
		}
		buildTree("", ROOT, ALL, records, attrs);
		
		XMLWriter writer = null;
		try {
			OutputStream out = new FileOutputStream(new File(outFilePath));
			writer = new XMLWriter(out, OutputFormat.createPrettyPrint());
			writer.write(doc);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
	}
	
	private Integer findAttrNameIndex(String attrName){
		for(int i=0;i<attrNames.size();++i)
			if(attrNames.get(i).equals(attrName))
				return i;
		return null;
	}

	
	private String getNotCoverMostClass(){
		int classNum[] = new int[attrValues.get(decidx).size()];
		for(int i=0;i<attrValues.get(decidx).size();++i)
			classNum[i] = 0;
		for(int i=0;i<flags.length;++i){
			if(flags[i] == 0)
				classNum[attrNames.indexOf(data.get(i).get(decidx))]++;
		}
		int max = 0;
		int idx = -1;
		for(int i=0;i<classNum.length;++i)
			if(max < classNum[i]){
				max = classNum[i];
				idx = i;
			}
		return attrValues.get(decidx).get(idx);
	}
	
	/**
	 * find the most class in the set
	 * @param set
	 * @return
	 */
	private String getMostProbabilityClass(List<Integer> set){
		Map<String, Integer> map = new HashMap<String, Integer>();
		String res = null;
		Integer most = 0;
		for(Integer idx: set){
			String key = data.get(idx).get(decidx);
			if(!map.containsKey(key))
				map.put(key, 1);
			else
				map.put(key, map.get(key) + 1);
			
			if(map.get(key) > most){
				most = map.get(key);
				res = key;
			}
		}
		mostClass = res;
		return res;
		
	}
	
	private String getMostProbabilityClass(List<Integer> set){
		Map<String, Integer> map = new HashMap<String, Integer>();
		String res = null;
		Integer most = 0;
		for(Integer idx: set){
			String key = data.get(idx).get(decidx);
			if(!map.containsKey(key))
				map.put(key, 1);
			else
				map.put(key, map.get(key) + 1);
			if(map.get(key) > most){
				most = map.get(key);
				res = key;
			}
		}
		mostClass = res;
		return res;
		
	}
	
	private void buildTree(String xpath, String preEleName, String preVal, List<Integer> set, ArrayList<String> attrs){
		
		String newXpath = xpath + "/" + preEleName + "[@" + VALUE + "='" + preVal + "']";
		@SuppressWarnings("rawtypes")
		List nodes = root.selectNodes(newXpath);
		
		LOG.debug(newXpath);
		Element ele = null;
		Iterator it = nodes.iterator();
		while(it.hasNext()){
			ele = (Element) it.next();
			if(ele.attributeValue(VALUE).equals(preVal))
				break; 
		}
		
		if(set.size() == 0){
			if(mostClass == null){
				List<Integer> records = new ArrayList<Integer>(data.size());
				for(int i=0;i<data.size();++i)
					records.add(i);
				mostClass = getMostProbabilityClass(records);
			}
			ele.setText(mostClass);
			return;
		}
		
		//LOG.debug("element=" + ele);
		//LOG.debug("eleName=" + preEleName +"\tval=" + preVal);
		Res res = pureInfo(set);
		if(res.isPure){
			ele.addText(res.clazz);
			for(Integer idx: set)
				flags[idx] = 1;
			return;
		}
		
		if(attrs==null || attrs.size()==0){
			String className = getMostProbabilityClass(set);
			ele.addText(className);
			return;
		}
		
		if(attrs==null || attrs.size()==0){
			String className = getMostProbabilityClass(set);
			ele.addText(className);
			return;
		}
		
		double minEntropy = Double.MAX_VALUE;
		int attrIdx = -1;
		String attr = null;
		for(int i=0;i<attrs.size();++i){
			String tmpAttr = attrs.get(i);
			int tmpAttrIdx = findAttrNameIndex(tmpAttr);
			double entropy = calcExpectEntropyByAttr(tmpAttrIdx, set);
			if(entropy < minEntropy){
				minEntropy = entropy;
				attr = tmpAttr;
				attrIdx = findAttrNameIndex(attr);//attrNames的索引不一定与可选属性集的索引相同
			}
		}
		
		ArrayList<String> remainAttrs = (ArrayList<String>) attrs.clone();
		for(int i=0;i<remainAttrs.size();++i){
			if(remainAttrs.get(i).equals(attr)){
				remainAttrs.remove(i);
				break;
			}
		}
		if(attrIdx == -1){
<<<<<<< HEAD
			ele.setText(getNotCoverMostClass());
=======
			ele.setText(getMostProbabilityClass(set));
>>>>>>> 199a058cec47e7edd6e3238508d232ec1115ec5d
			return;
		}
		LOG.debug("attrs="+attrs + "\tattr="+attr + "\tattrIdx="+attrIdx);
	
		 List<ArrayList<Integer>> subsets = new ArrayList<ArrayList<Integer>>(attrValues.get(attrIdx).size());
		for(int i=0;i<attrValues.get(attrIdx).size();++i){
			subsets.add(new ArrayList<Integer>());
		}
		
		
		for(Integer idx: set){
			List<String> record = data.get(idx);
			int attrValIndex = findAttrValueIndex(attrIdx, record.get(attrIdx));
			subsets.get(attrValIndex).add(idx);
		}
		
		/*
		for(int i=0;i<subsets.size();++i){
			for(int j=0;j<subsets.get(i).size();++j)
				System.out.println(data.get(subsets.get(i).get(j)));
			System.out.println("+++++++++++++++++++++++++");
		}*/
		
		List<String> values = attrValues.get(attrIdx);
		LOG.debug(attr + "=" + values + "attrIdx=" + attrIdx);
		for(int i=0;i<values.size();++i){
			if(subsets.get(i).size() != 0){
				ele.addElement(attr).addAttribute(VALUE, values.get(i));
				buildTree(newXpath, attr, values.get(i), subsets.get(i), (ArrayList<String>) remainAttrs.clone());
			}
		}
	}

}













