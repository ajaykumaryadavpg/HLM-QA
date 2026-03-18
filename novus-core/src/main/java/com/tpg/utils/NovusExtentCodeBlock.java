package com.tpg.utils;

import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import org.apache.commons.lang3.StringUtils;

public class NovusExtentCodeBlock implements Markup {

    private final String code;
    private String title;
    private ExtentColor titleColor;

    private NovusExtentCodeBlock(String code) {
        this.code = code;
    }

    public static NovusExtentCodeBlock withCode(String code) {
        return new NovusExtentCodeBlock(code);
    }

    public NovusExtentCodeBlock withTitle(String title, ExtentColor color) {
        this.title = title;
        this.titleColor = color == null ? ExtentColor.BLUE : color;
        return this;
    }

    @Override
    public String getMarkup() {
        StringBuilder markup = new StringBuilder("<div class=\"card ");
        if (StringUtils.isNotEmpty(title)) {
            markup.append("materialize-")
                .append(titleColor.name().toLowerCase())
                .append("\"><div class=\"card-header label white-text\">")
                .append(title)
                .append("</div>");
        } else {
            markup.append("\">");
        }
        markup.append("<div class=\"card-panel nm-v\"><pre>")
            .append(code)
            .append("</pre></div></div>");
        return markup.toString();
    }
}
