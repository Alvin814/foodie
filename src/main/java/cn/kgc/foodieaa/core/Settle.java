package cn.kgc.foodieaa.core;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

public class Settle extends IO{
    private List<String> lstDate;
    private Map<String,Double> map;
    private Pattern patDate;

    public Settle() {
        lstDate = new ArrayList<>();
        map = new HashMap<>();
        patDate = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    }

    public void deal(){
        List<String> lines = readData();
        if(lines.isEmpty()){
            log.info("try to settle but no original data");
            return;
        }
        settleLines(lines);
        Map<String, String> mapMember = readMember();
        String dateInfo = getLstDate();
        lines.clear();
        String settleDate = sdf.format(new Date());
        lines.add("SETTLE [ "+dateInfo+" ] at "+settleDate);
        for (Map.Entry<String, Double> e : map.entrySet()) {
            String[] s = e.getKey().split("->");
            lines.add(mapMember.get(s[0])+"->"+mapMember.get(s[1])+"\t"+e.getValue());
        }
        settleRecord(lines);
        log.info("settle successfully "+dateInfo+" at "+settleDate);
        lines.clear();
        mapMember.clear();
    }

    private String getLstDate() {
        return MessageFormat.format(
                "{0}~{1}",
                lstDate.get(0),
                lstDate.get(lstDate.size()-1));
    }

    private void settleLines(List<String> lines){
        Iterator<String> it = lines.iterator();
        while (it.hasNext()) {
            String line = it.next();
            if(patDate.matcher(line).matches()){
                lstDate.add(line);
            }else{
                settleLine(line);
            }
        }
    }

    // 2,1_7_8_11_12,120
    private void settleLine(String line){
        String[] split = line.split(",");
        String payId = split[0];
        String[] shareIds = split[1].split("_");
        double avg = Math.round(Double.parseDouble(split[2])/(shareIds.length+1));
        for (String shareId : shareIds) {
            put(shareId+"->"+payId,avg);
        }
    }

    private void put(String item,Double money){
        map.put(item , (map.containsKey(item) ? map.get(item) : 0) + money);
    }
}
