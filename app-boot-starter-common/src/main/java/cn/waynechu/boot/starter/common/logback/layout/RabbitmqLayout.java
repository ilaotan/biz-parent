package cn.waynechu.boot.starter.common.logback.layout;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * @author zhuwei
 * @date 2018/12/27 18:52
 */
@Slf4j
public class RabbitmqLayout extends LayoutBase<ILoggingEvent> {
    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static List<String> optionList = Collections.singletonList("full");

    private static ThrowableProxyConverter converter = new ThrowableProxyConverter();

    protected String machineName;

    protected String localAddress;

    static {
        converter.setOptionList(optionList);
        converter.start();
    }

    public RabbitmqLayout() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            machineName = address.getHostName();
            localAddress = address.getHostAddress();
        } catch (UnknownHostException e) {
            this.addError("RabbitMQLayout无法获取machineName和localAddress", e);
        }
    }

    @Override
    public String doLayout(ILoggingEvent iLoggingEvent) {
        return buildELKFormat(iLoggingEvent);
    }

    private String buildELKFormat(ILoggingEvent iLoggingEvent) {
        JSONObject root = new JSONObject();

        // write mdc fields
        writeMdc(root, iLoggingEvent);
        // write basic fields
        writeBasic(root, iLoggingEvent);
        // write throwable fields
        writeThrowable(root, iLoggingEvent);
        // write timeToken fields
        writeTimeToken(root, iLoggingEvent);

        return root.toString();
    }

    private void writeMdc(JSONObject json, ILoggingEvent event) {
        if (event.getMDCPropertyMap() != null) {
            json.putAll(event.getMDCPropertyMap());
        }
    }

    private void writeBasic(JSONObject json, ILoggingEvent event) {
        json.put("machineName", machineName);
        json.put("localAddress", localAddress);
        json.put("threadName", event.getThreadName());
        json.put("level", event.getLevel().toString());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeStamp()),
                ZoneId.systemDefault());
        json.put("time", dateTimeFormatter.format(localDateTime));
        json.put("message", event.getFormattedMessage());
        json.put("logger", event.getLoggerName());
        json.put("uri", "");
        json.put("timetoken", 0);
    }

    private void writeThrowable(JSONObject json, ILoggingEvent event) {
        IThrowableProxy iThrowableProxy = event.getThrowableProxy();
        if (iThrowableProxy instanceof ThrowableProxy) {
            ThrowableProxy throwableProxy = (ThrowableProxy) iThrowableProxy;
            Throwable t = throwableProxy.getThrowable();
            JSONObject throwable = new JSONObject();

            throwable.put("message", t.getMessage());
            throwable.put("className", t.getClass().getCanonicalName());
            throwable.put("stackTrace", writeStackTrace(event));
            json.put("throwable", throwable);
        }
    }

    private String writeStackTrace(ILoggingEvent event) {
        StringBuilder stringBuilder = new StringBuilder(2048);
        IThrowableProxy proxy = event.getThrowableProxy();
        if (proxy != null) {
            stringBuilder.append(converter.convert(event));
            stringBuilder.append(CoreConstants.LINE_SEPARATOR);
        }
        return stringBuilder.toString();
    }

    private void writeTimeToken(JSONObject json, ILoggingEvent event) {
        try {
            String formattedMessage = event.getFormattedMessage();
            JSONObject parseObject = new JSONObject();
            if (formattedMessage.contains("uri") && formattedMessage.contains("timetoken")) {
                String jsonStr = formattedMessage.substring(formattedMessage.indexOf("{"));
                if (StringUtils.isNotBlank(jsonStr)) {
                    parseObject = JSONObject.parseObject(jsonStr);
                }
            }
            json.put("uri", parseObject.get("uri") == null ? "" : parseObject.get("uri"));
            json.put("timetoken", parseObject.get("timetoken") == null ? 0 : parseObject.get("timetoken"));
        } catch (Exception e) {
            // do nothing here.
        }
    }
}