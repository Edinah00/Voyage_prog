package ui;

import models.CheminItem;
import javax.swing.*;
import java.awt.*;

public class CheminCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof CheminItem) {
            CheminItem item = (CheminItem) value;
            
            String statusIcon = item.isCarburantSuffisant() ? "OK" : "X";
            
            String html = String.format(
                "<html><b>%s</b> %s<br/>" +
                "Distance: %.1f km | Carburant: <font color='%s'>%.1f L %s</font></html>",
                statusIcon,
                item.toString(),
                item.getDistance(),
                item.isCarburantSuffisant() ? "green" : "red",
                item.getCarburantNecessaire(),
                item.isCarburantSuffisant() ? "" : "(INSUFFISANT)"
            );
            
            setText(html);
            
            if (!item.isCarburantSuffisant()) {
                setForeground(new Color(128, 128, 128));
            }
        }
        return this;
    }
}