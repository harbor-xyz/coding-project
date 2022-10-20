package com.harbor.calendly.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.harbor.calendly.models.CalendarSlot;
import com.harbor.calendly.models.DOWSlot;
import com.harbor.calendly.models.DayOfWeek;
import com.harbor.calendly.models.SlotType;

public class CommonUtils {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    public static List<DOWSlot> getPlatformDOWSlots() {
        List<DOWSlot> result = new ArrayList<>();
        for (DayOfWeek dow : DayOfWeek.getWeekdays()) {
            DOWSlot dowSlot = new DOWSlot();
            dowSlot.setDow(dow);
            List<CalendarSlot> freeSlots = new ArrayList<>();
            CalendarSlot cs = new CalendarSlot(32400, 61200, SlotType.FREE);
            freeSlots.add(cs);
            dowSlot.setFreeSlots(freeSlots);
            logger.info("Slot is : {}", dowSlot);
            result.add(dowSlot);
        }
        return result;
    }

    public void merge(Object obj, Object update) {
        if (!obj.getClass().isAssignableFrom(update.getClass())) {
            return;
        }

        Method[] methods = obj.getClass().getMethods();

        for (Method fromMethod : methods) {
            if (fromMethod.getDeclaringClass().equals(obj.getClass())
                    && fromMethod.getName().startsWith("get")) {

                String fromName = fromMethod.getName();
                String toName = fromName.replace("get", "set");

                try {
                    Method toMetod = obj.getClass().getMethod(toName, fromMethod.getReturnType());
                    Object value = fromMethod.invoke(update, (Object[]) null);
                    if (value != null) {
                        toMetod.invoke(obj, value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
