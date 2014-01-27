package de.fraunhofer.iais.kd.biovel.shim.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;

public class ShimDateConverter {
    
    private static final Logger LOG = Logger.getLogger(ShimDateConverter.class.getName());
    
    private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
        put("^\\d{8}$", "yyyyMMdd");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
        put("^\\d{1,2}/\\d{1,2}/\\d{2}$", "MM/dd/yy");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
        put("^\\d{12}$", "yyyyMMddHHmm");
        put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
        put("^\\d{14}$", "yyyyMMddHHmmss");
        put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\w{1}\\d{1,2}:\\d{2}:\\d{2}\\w{1}$", "yyyy-MM-dd'T HH:mm:ss'Z");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
    }};
    
    public String convert(String dateString){
        
        String newDate = "";
        
        String dateFormat = determineDateFormat(dateString);
        if (dateFormat == null) {
            LOG.info("unknown date format ->" + dateString);
            return "";
        }
        
        System.out.println("\ndateString: " + dateString);
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        SimpleDateFormat newSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormat.setLenient(false); // Don't automatically convert invalid date.
        
        
        try {
            Date date = simpleDateFormat.parse(dateString);
            newDate = newSimpleDateFormat.format(date);
            
//            System.out.println("new: "+ newDate);
            
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            
            String scResult = "";
            if(dateFormat.contains("-")){
                if(dateFormat.equals("yyyy-MM-dd")||dateFormat.equals("yyyy-MM-dd'T HH:mm:ss'Z")){
                    scResult = dateString;
                } else if(dateFormat.equals("dd-MM-yyyy")){
                    Scanner scanner = new Scanner( dateString ).useDelimiter( "-" );
                    while ( scanner.hasNext() ){
//                        System.out.println( "Scanner next: " + scanner.next() );
                        scResult = scanner.next()+"-"+scResult;
                        
                    }
                    scanner.close();
                    scResult = scResult.substring(0, scResult.length()-1);
                }
                
            }else if(dateFormat.contains("/")){
                if(dateFormat.equals("yyyy/MM/dd")){
                    Scanner scanner = new Scanner( dateString ).useDelimiter( "/" );
                    while ( scanner.hasNext() ){
//                        System.out.println( "Scanner next: " + scanner.next() );
                        scResult = scResult+"-"+scanner.next();
                        
                    }
                    scanner.close();
                    scResult = scResult.substring(1, scResult.length());
                    
                }else if(dateFormat.equals("MM/dd/yyyy")){
                    Scanner scanner = new Scanner( dateString ).useDelimiter( "/" );
                    String[] tempResult = new String[3];
                    int ix = 0;
                    while ( scanner.hasNext() ){
//                        System.out.println( "Scanner next: " + scanner.next() );
//                        scResult = scResult+"-"+scanner.next();
                        tempResult[ix]=scanner.next();
                        ix++;
                        
                    }
                    scanner.close();
                    scResult = tempResult[2] + "-" + tempResult[0] + "-" + tempResult[1];
                }
            }else {
                scResult = "";
            }
            
            System.out.println("scResult: " + scResult);
            
            newDate = scResult;
            
//            e.printStackTrace();
            
        }
        
        return newDate;
        
    }
    
    /**
     * Determine SimpleDateFormat pattern matching with the given date string. Returns null if
     * format is unknown. You can simply extend DateUtil with more formats if needed.
     * @param dateString The date string to determine the SimpleDateFormat pattern for.
     * @return The matching SimpleDateFormat pattern, or null if format is unknown.
     * @see SimpleDateFormat
     */
    private String determineDateFormat(String dateString) {
        for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
            if (dateString.toLowerCase().matches(regexp)) {
                return DATE_FORMAT_REGEXPS.get(regexp);
            }
        }
        return null; // Unknown format.
    }

}
