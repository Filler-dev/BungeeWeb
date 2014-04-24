package io.github.dead_i.bungeeweb.api;

import com.google.gson.Gson;
import io.github.dead_i.bungeeweb.APICommand;
import io.github.dead_i.bungeeweb.BungeeWeb;
import net.md_5.bungee.api.plugin.Plugin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class GetStats extends APICommand {
    private Gson gson = new Gson();

    public GetStats() {
        super("getstats", 1);
    }


    @Override
    public void execute(Plugin plugin, HttpServletRequest req, HttpServletResponse res, String[] args) throws IOException, SQLException {
        String param = req.getParameter("limit");
        int limit = 50;
        if (param != null && BungeeWeb.isNumber(param)) limit = Integer.parseInt(param);

        if (limit > 500) {
            res.getWriter().print("{ \"error\": \"Attempted to fetch " + limit + " records. The number of records you request is capped at 500 for security reasons.\" }");
            return;
        }

        ResultSet rs = BungeeWeb.getDatabase().createStatement().executeQuery("SELECT * FROM `" + BungeeWeb.getConfig().getString("database.prefix") + "stats` LIMIT " + limit);

        int inc = BungeeWeb.getConfig().getInt("server.statscheck");
        int current = (int) (System.currentTimeMillis() / 1000);
        current = current - (current % inc);

        HashMap<Integer, Object> records = new HashMap<Integer, Object>();
        while (rs.next()) {
            HashMap<String, Object> record = new HashMap<String, Object>();
            record.put("playercount", rs.getInt("playercount"));
            record.put("maxplayers", rs.getInt("maxplayers"));
            record.put("activity", rs.getInt("activity"));
            records.put(rs.getInt("time"), record);
        }

        HashMap<Integer, Object> out = new HashMap<Integer, Object>();
        for (int i = 0; i < limit; i++) {
            if (records.containsKey(current)) out.put(current, out.get(current));
        }

        res.getWriter().print(gson.toJson(out));
    }
}
