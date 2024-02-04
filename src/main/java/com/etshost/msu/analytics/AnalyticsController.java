package com.etshost.msu.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.dbcp2.BasicDataSource;

@RequestMapping("/analytics")
@RestController
public class AnalyticsController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    BasicDataSource dataSource;
    
    @RequestMapping(value = "/{startDate}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> analytics(@PathVariable("startDate") String startDate) {
        try {
            
            Connection conn = dataSource.getConnection();
            StringBuilder sb = new StringBuilder();

            // Views
            PreparedStatement ps = conn.prepareStatement("SELECT\r\n" + //
            "\tCONCAT(EXTRACT (YEAR FROM viewing.starttime), '-', LPAD((EXTRACT (MONTH FROM viewing.starttime))::text, 2, '0')) AS date,\r\n" + //
            "\tget_entity_type(viewing.target_id) AS type,\r\n" + //
            "\tCOUNT(*) AS count\r\n" + //
            "FROM public.viewing\r\n" + //
            "WHERE viewing.starttime >= ?::date\r\n" + //
            "GROUP BY date, type\r\n" + //
            "ORDER BY date ASC, type ASC;");
            ps.setString(1, startDate);
            logger.info(ps.toString());
            ResultSet rs = ps.executeQuery();
            sb.append("Views\r\nDate,Type,Count\r\n");
            while(rs.next()) {
                sb.append(rs.getString(1) + "," + rs.getString(2) + ",");
                sb.append(rs.getInt(3));
                sb.append("\r\n");
            }


            // Creates
            ps = conn.prepareStatement("SELECT \r\n" +
            "CONCAT(EXTRACT (YEAR FROM entity.created), '-', LPAD((EXTRACT (MONTH FROM entity.created))::text, 2, '0')) AS date,\r\n" +
            "    get_entity_type(entity.id) AS type,\r\n" +
            "    COUNT(*) AS count\r\n" +
            "FROM public.entity\r\n" +
            "LEFT JOIN authenticationrecord\r\n" +
            "    ON entity.id = authenticationrecord.id\r\n" +
            "WHERE entity.created >= ?::date AND get_entity_type(entity.id) IN ('Deal', 'Tip', 'Review', 'Recipe', 'User')\r\n" +
            "GROUP BY date, type\r\n" +
            "ORDER BY date ASC, type ASC;");
            ps.setString(1, startDate);
            logger.info(ps.toString());
            rs = ps.executeQuery();
            sb.append("\r\nCreates\r\nDate,Type,Count\r\n");
            while(rs.next()) {
                sb.append(rs.getString(1) + "," + rs.getString(2) + ",");
                sb.append(rs.getInt(3));
                sb.append("\r\n");
            }


            // Likes
            ps = conn.prepareStatement("SELECT \r\n" +
            "CONCAT(EXTRACT (YEAR FROM reaction.starttime), '-', LPAD((EXTRACT (MONTH FROM reaction.starttime))::text, 2, '0')) AS date,\r\n" +
            "    get_entity_type(reaction.target_id) AS type,\r\n" +
            "    COUNT(*) AS count\r\n" +
            "FROM public.reaction\r\n" +
            "WHERE reaction.starttime >= ?::date AND get_entity_type(reaction.target_id) IN ('Deal', 'Tip', 'Review', 'Recipe')\r\n" +
            "GROUP BY date, type\r\n" +
            "ORDER BY date ASC, type ASC;");
            ps.setString(1, startDate);
            logger.info(ps.toString());
            rs = ps.executeQuery();
            sb.append("\r\nLikes\r\nDate,Type,Count\r\n");
            while(rs.next()) {
                sb.append(rs.getString(1) + "," + rs.getString(2) + ",");
                sb.append(rs.getInt(3));
                sb.append("\r\n");
            }


            // Comments
            ps = conn.prepareStatement("SELECT \r\n" +
            "CONCAT(EXTRACT (YEAR FROM entity.created), '-', LPAD((EXTRACT (MONTH FROM entity.created))::text, 2, '0')) AS date,\r\n" +
            "    get_entity_type(comment.target_id) AS type,\r\n" +
            "    COUNT(*) AS count\r\n" +
            "FROM public.comment\r\n" +
            "LEFT JOIN public.entity\r\n" +
            "    ON comment.id = entity.id\r\n" +
            "WHERE entity.created >= ?::date AND get_entity_type(comment.target_id) IN ('Deal', 'Tip', 'Review', 'Recipe')\r\n" +
            "GROUP BY date, type\r\n" +
            "ORDER BY date ASC, type ASC;");
            ps.setString(1, startDate);
            logger.info(ps.toString());
            rs = ps.executeQuery();
            sb.append("\r\nComments\r\nDate,Type,Count\r\n");
            while(rs.next()) {
                sb.append(rs.getString(1) + "," + rs.getString(2) + ",");
                sb.append(rs.getInt(3));
                sb.append("\r\n");
            }

            MultiValueMap<String,String> headers = new LinkedMultiValueMap<String, String>();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");
            LocalDate localDate = LocalDate.now();
            String endDate = dtf.format(localDate);
            headers.add("Content-Disposition", "attachment; filename=" + startDate + "_to_" + endDate + "_stats.csv");

            return new ResponseEntity<String>(sb.toString(), headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity<String>(e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
            // for (StackTraceElement trace : e.getStackTrace()) {
            //     logger.error(trace.toString());
            // }
            // TODO: handle exception
        }
	}
}
