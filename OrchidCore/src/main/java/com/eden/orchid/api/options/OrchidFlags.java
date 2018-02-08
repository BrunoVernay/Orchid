package com.eden.orchid.api.options;

import com.caseyjbrooks.clog.Clog;
import com.eden.common.util.EdenPair;
import com.eden.common.util.EdenUtils;
import com.google.inject.AbstractModule;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.name.Names;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.Getter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OrchidFlags {

    static OrchidFlags instance;

    public static OrchidFlags getInstance() {
        if (instance == null) {
            List<OrchidFlag> orchidFlags = new ArrayList<>();

            FastClasspathScanner scanner = new FastClasspathScanner();
            scanner.matchSubclassesOf(OrchidFlag.class, (matchingClass) -> {
                try {
                    OrchidFlag option = matchingClass.newInstance();
                    if (option != null) {
                        orchidFlags.add(option);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            });
            scanner.scan();
            instance = new OrchidFlags(orchidFlags);
        }
        return instance;
    }

    @Getter private Collection<OrchidFlag> flags;
    private JSONObject parsedFlagsData;

    private List<OrchidFlag> missingRequiredFlags = new ArrayList<>();
    private Map<String, EdenPair<OrchidFlag.FlagType, Object>> parsedFlags;

    OrchidFlags(Collection<OrchidFlag> flags) {
        this.flags = flags;
        this.parsedFlags = new HashMap<>();
    }

    public AbstractModule parseFlags(Map<String, String[]> flagsMap) {
        for (OrchidFlag flag : flags) {
            String[] options = flagsMap.getOrDefault("-" + flag.getFlag(), null);
            if (validateOptionLength(flag, options)) {
                parseFlag(flag, options);
            }
            else if(flag.isRequired() && options == null) {
                parseFlag(flag, new String[] {"-" + flag.getFlag(), flag.getDefaultValue().toString() });
            }
        }

        if (missingRequiredFlags.size() > 0) {
            for (OrchidFlag flag : missingRequiredFlags) {
                Clog.e("Missing required flag: -#{$1} - #{$2}", flag.getFlag(), flag.getDescription());
            }
            throw new RuntimeException("Some required flags are missing, cannot continue.");
        }

        return new AbstractModule() {
            @Override
            protected void configure() {
                parsedFlagsData = new JSONObject();

                for (Map.Entry<String, EdenPair<OrchidFlag.FlagType, Object>> entry : parsedFlags.entrySet()) {
                    AnnotatedBindingBuilder binder = null;

                    switch (entry.getValue().first) {
                        case BOOLEAN:
                            binder = bind(Boolean.class);
                            parsedFlagsData.put(entry.getKey(), (boolean) entry.getValue().second);
                            break;
                        case DOUBLE:
                            binder = bind(Double.class);
                            parsedFlagsData.put(entry.getKey(), (double) entry.getValue().second);
                            break;
                        case INTEGER:
                            binder = bind(Integer.class);
                            parsedFlagsData.put(entry.getKey(), (int) entry.getValue().second);
                            break;
                        case STRING:
                            binder = bind(String.class);
                            parsedFlagsData.put(entry.getKey(), (String) entry.getValue().second);
                            break;
                        case STRING_ARRAY:
                            binder = bind(String[].class);
                            parsedFlagsData.put(entry.getKey(), (String[]) entry.getValue().second);
                            break;
                    }

                    binder.annotatedWith(Names.named(entry.getKey()))
                          .toInstance(entry.getValue().second);
                }
            }
        };
    }

    private boolean validateOptionLength(OrchidFlag flag, String[] options) {
        if (options != null) {
            if (options.length == flag.optionLength()) {
                return true;
            }

            Clog.e("'-#{$1}' flag should be of length #{$2}: was given #{$3}", flag.getFlag(), flag.optionLength(), options.length);
        }
        return false;
    }

    private void parseFlag(OrchidFlag flag, String[] options) {
        Object flagValue = null;

        if (EdenUtils.isEmpty(options) && flag.isRequired()) {
            missingRequiredFlags.add(flag);
        }
        else {
            flagValue = flag.parseFlag(options);
            if (flagValue == null) {
                if (flag.isRequired()) {
                    missingRequiredFlags.add(flag);
                }
                else {
                    flagValue = flag.getDefaultValue();
                }
            }
        }

        if (validateFlagType(flag, flagValue)) {
            parsedFlags.put(flag.getFlag(), new EdenPair<>(flag.getFlagType(), flagValue));
        }
        else if (flag.isRequired()) {
            missingRequiredFlags.add(flag);
        }
    }

    private boolean validateFlagType(OrchidFlag flag, Object flagValue) {
        if (flagValue == null) {
            return false;
        }

        switch (flag.getFlagType()) {
            case BOOLEAN:
                return (flagValue instanceof Boolean);
            case DOUBLE:
                return (flagValue instanceof Float) || (flagValue instanceof Double);
            case INTEGER:
                return (flagValue instanceof Integer) || (flagValue instanceof Long);
            case STRING:
                return (flagValue instanceof String);
            case STRING_ARRAY:
                return (flagValue instanceof String[]);
            default:
                return false;
        }
    }

    public String getString(String key) {
        if (parsedFlags.containsKey(key) && parsedFlags.get(key).first.equals(OrchidFlag.FlagType.STRING)) {
            return (String) parsedFlags.get(key).second;
        }
        return null;
    }

    public String[] getStringArray(String key) {
        if (parsedFlags.containsKey(key) && parsedFlags.get(key).first.equals(OrchidFlag.FlagType.STRING_ARRAY)) {
            return (String[]) parsedFlags.get(key).second;
        }
        return null;
    }

    public int getInteger(String key) {
        if (parsedFlags.containsKey(key) && parsedFlags.get(key).first.equals(OrchidFlag.FlagType.INTEGER)) {
            return (int) parsedFlags.get(key).second;
        }
        return 0;
    }

    public double getDouble(String key) {
        if (parsedFlags.containsKey(key) && parsedFlags.get(key).first.equals(OrchidFlag.FlagType.DOUBLE)) {
            return (double) parsedFlags.get(key).second;
        }
        return 0;
    }

    public boolean getBoolean(String key) {
        if (parsedFlags.containsKey(key) && parsedFlags.get(key).first.equals(OrchidFlag.FlagType.BOOLEAN)) {
            return (boolean) parsedFlags.get(key).second;
        }
        return false;
    }

    public JSONObject getData() {
        return parsedFlagsData;
    }
}
