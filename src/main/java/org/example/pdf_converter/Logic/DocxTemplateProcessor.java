package org.example.pdf_converter.Logic;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Text;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.docx4j.toc.TocHelper.getAllElementsFromObject;

@Component
public class DocxTemplateProcessor {
    public static void process(WordprocessingMLPackage wordMLPackage, Map<String, String> data) {

        // ✅ 1. Main Document
        replaceInPart(wordMLPackage.getMainDocumentPart(), data);

        // ✅ 2. Headers & Footers (ALL SECTIONS)
        for (SectionWrapper section : wordMLPackage.getDocumentModel().getSections()) {

            if (section.getHeaderFooterPolicy() != null) {

                var hf = section.getHeaderFooterPolicy();

                replaceInPart(hf.getDefaultHeader(), data);
                replaceInPart(hf.getFirstHeader(), data);
                replaceInPart(hf.getEvenHeader(), data);

                replaceInPart(hf.getDefaultFooter(), data);
                replaceInPart(hf.getFirstFooter(), data);
                replaceInPart(hf.getEvenFooter(), data);
            }
        }

    }

    // 🔥 UNIVERSAL PART HANDLER
    private static void replaceInPart(Object part, Map<String, String> data) {

        if (part == null) return;

        // 🔥 1. Paragraph-based (smart replace)
        List<Object> paragraphs = getAllElementsFromObject(part, org.docx4j.wml.P.class);

        for (Object paragraph : paragraphs) {
            replaceInParagraph(paragraph, data);
        }


    }

    // 🔥 SMART PARAGRAPH REPLACER (Split-safe)
    private static void replaceInParagraph(Object paragraph, Map<String, String> data) {

        List<Object> texts = getAllElementsFromObject(paragraph, Text.class);

        if (texts == null || texts.isEmpty()) return;

        StringBuilder fullText = new StringBuilder();

        // ✅ 1. Combine all text nodes
        for (Object obj : texts) {

            if (obj instanceof JAXBElement) {
                obj = ((JAXBElement<?>) obj).getValue();
            }

            if (obj instanceof Text) {
                fullText.append(((Text) obj).getValue());
            }
        }

        String combined = fullText.toString();

        // ✅ 2. Replace placeholders
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = "<<" + entry.getKey() + ">>";
            combined = combined.replace(key, entry.getValue());
        }

        // ✅ 3. Set back (only first node)
        boolean first = true;

        for (Object obj : texts) {

            if (obj instanceof JAXBElement) {
                obj = ((JAXBElement<?>) obj).getValue();
            }

            if (obj instanceof Text) {

                Text t = (Text) obj;

                if (first) {
                    t.setValue(combined);
                    first = false;
                } else {
                    t.setValue("");
                }
            }
        }
    }
}
