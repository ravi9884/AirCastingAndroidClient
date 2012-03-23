package pl.llp.aircasting.activity.adapter;

import android.app.Activity;
import android.widget.SimpleAdapter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import pl.llp.aircasting.R;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.model.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class StreamAdapter extends SimpleAdapter {
    public static final String TITLE = "title";
    public static final String NOW = "now";
    public static final String AVERAGE = "average";
    public static final String PEAK = "peak";
    public static final String PEAK_LABEL = "peak_label";
    public static final String AVG_LABEL = "avg_label";
    public static final String NOW_LABEL = "now_label";
    public static final String VERY_LOW = "veryLow";
    public static final String LOW = "low";
    public static final String MID = "mid";
    public static final String HIGH = "high";
    public static final String VERY_HIGH = "veryHigh";

    private static final String[] FROM = new String[]{
            TITLE, NOW, AVERAGE, PEAK,
            NOW_LABEL, AVG_LABEL, PEAK_LABEL,
            VERY_LOW, LOW, MID, HIGH, VERY_HIGH
    };
    private static final int[] TO = new int[]{
            R.id.title, R.id.db_now, R.id.db_avg, R.id.db_peak,
            R.id.now_label, R.id.avg_label, R.id.peak_label,
            R.id.top_bar_very_low, R.id.top_bar_low, R.id.top_bar_mid, R.id.top_bar_high, R.id.top_bar_very_high
    };

    private List<Map<String, String>> data;
    private Map<String, Map<String, String>> sensors = newHashMap();

    private Activity context;
    private SessionManager sessionManager;
    EventBus eventBus;

    public StreamAdapter(Activity context, List<Map<String, String>> data, EventBus eventBus, SessionManager sessionManager) {
        super(context, data, R.layout.stream, FROM, TO);
        this.data = data;
        this.eventBus = eventBus;
        this.context = context;
        this.sessionManager = sessionManager;
    }

    public void start() {
        eventBus.register(this);
    }

    public void stop() {
        eventBus.unregister(this);
    }

    @Subscribe
    public void onEvent(final SensorEvent event) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                update(event);
            }
        });
    }

    private void update(SensorEvent event) {
        String name = event.getSensorName();
        if (!sensors.containsKey(name)) {
            HashMap<String, String> map = new HashMap<String, String>();
            sensors.put(name, map);
            data.add(map);
        }
        Map<String, String> map = sensors.get(name);

        map.put(TITLE, title(event));

        map.put(NOW, String.valueOf((int) sessionManager.getNow(name)));
        map.put(AVERAGE, String.valueOf((int) sessionManager.getAvg(name)));
        map.put(PEAK, String.valueOf((int) sessionManager.getPeak(name)));
        map.put(PEAK_LABEL, label(R.string.peak_label_template, event));
        map.put(NOW_LABEL, label(R.string.now_label_template, event));
        map.put(AVG_LABEL, label(R.string.avg_label_template, event));
        
        map.put(VERY_LOW, String.valueOf(event.getVeryLow()));
        map.put(LOW, String.valueOf(event.getLow()));
        map.put(MID, String.valueOf(event.getMid()));
        map.put(HIGH, String.valueOf(event.getHigh()));
        map.put(VERY_HIGH, String.valueOf(event.getVeryHigh()));

        notifyDataSetChanged();
    }

    private String title(SensorEvent event) {
        StringBuilder builder = new StringBuilder();

        return builder.append(event.getMeasurementType())
                .append(" - ")
                .append(event.getSensorName())
                .toString();
    }

    private String label(int templateId, SensorEvent event) {
        String template = context.getString(templateId);
        return String.format(template, event.getSymbol());
    }
}