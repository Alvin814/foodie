package cn.kgc.foodieaa.core;

import org.apache.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class IO {
    protected static Logger log = Logger.getLogger(IO.class);
    protected static final SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
    private Properties prop;
    private static final String CONFIG_PATH = "config/foodieaa.properties";

    public IO() {
        prop = new Properties();
        try {
            prop.load(new FileReader(CONFIG_PATH));
        } catch (IOException e) {
            log.error(e);
            System.exit(-1);
        }
    }

    protected void close(AutoCloseable...closes){
        for (AutoCloseable close : closes) {
            if (null!=close) {
                try {
                    close.close();
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    protected void writeConfig(long offset){
        prop.setProperty("lastOffset",offset+"");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(CONFIG_PATH);
            prop.store(fos,"LAST_MODIFIED "+sdf.format(new Date()));
            fos.flush();
            log.info("modify config item lastOffset successfully : "+offset);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("modify config item lastOffset in failure : "+offset);
        } finally {
            close(fos);
        }
    }

    protected List<String> readData(){
        long OFFSET = Long.parseLong(prop.getProperty("lastOffset"));
        final String DATA_PATH = prop.getProperty("dataDir")+"/records.log";
        RandomAccessFile raf = null;
        List<String> lst = new ArrayList<>(0);
        try {
            raf = new RandomAccessFile(DATA_PATH,"r");
            raf.seek(OFFSET);
            String line = null;
            while (null != (line = raf.readLine())) {
                OFFSET += line.getBytes().length+2;
                if(0==(line=line.trim()).length()) continue;
                lst.add(line);
            }
            writeConfig(OFFSET);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("random read original data in failure");
        } finally {
            close(raf);
        }
        return lst;
    }

    protected Map<String,String> readMember(){
        final String DATA_PATH = prop.getProperty("dataDir")+"/members.log";
        BufferedReader br = null;
        Map<String,String> map = new HashMap<>(0);
        try {
            br = new BufferedReader(new FileReader(DATA_PATH));
            String line = null;
            while (null != (line = br.readLine())) {
                line = line.trim();
                String[] s = line.split(",");
                map.put(s[0],s[1]);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("read member info in failure");
        }finally {
            close(br);
        }
        return map;
    }

    protected void settleRecord(List<String> records){
        BufferedWriter bw = null;
        final String DATA_PATH = prop.getProperty("dataDir")+"/settle.log";
        try {
            bw = new BufferedWriter(new FileWriter(DATA_PATH,true));
            for (String record : records) {
                bw.write(record);
                bw.newLine();
            }
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error("write settle result in failure");
        }finally {
            close(bw);
        }
    }
}
