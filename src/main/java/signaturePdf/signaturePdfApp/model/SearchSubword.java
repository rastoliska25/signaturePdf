package signaturePdf.signaturePdfApp.model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import signaturePdf.signaturePdfApp.Logging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class SearchSubword {

    List<Float> printSubwordsImprovedList(PDDocument document, String searchTerm) throws IOException {
        float x = 0F;
        float y = 0F;
        int pageNumber = 0;
        List<Float> listResult = new ArrayList<>();
        for (int page = 1; page <= document.getNumberOfPages(); page++) {
            List<TextPositionSequence> hits = findSubwordsImproved(document, page, searchTerm);
            for (TextPositionSequence hit : hits) {
                if (!searchTerm.equals(hit.toString()))
                    Logging.logger.info(" Problem finding signature mark: " + hit.toString());

                x = hit.getX();
                y = hit.getY();
                pageNumber = page;
            }
        }
        listResult.add(x);
        listResult.add(y);
        listResult.add((float) pageNumber);
        return listResult;
    }

    List<TextPositionSequence> findSubwordsImproved(PDDocument document, int page, String searchTerm) throws IOException {
        final List<TextPosition> allTextPositions = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                allTextPositions.addAll(textPositions);
                super.writeString(text, textPositions);
            }

            @Override
            protected void writeLineSeparator() throws IOException {
                if (!allTextPositions.isEmpty()) {
                    TextPosition last = allTextPositions.get(allTextPositions.size() - 1);
                    if (!" ".equals(last.getUnicode())) {
                        Matrix textMatrix = last.getTextMatrix().clone();
                        textMatrix.setValue(2, 0, last.getEndX());
                        textMatrix.setValue(2, 1, last.getEndY());
                        TextPosition separatorSpace = new TextPosition(last.getRotation(), last.getPageWidth(), last.getPageHeight(),
                                textMatrix, last.getEndX(), last.getEndY(), last.getHeight(), 0, last.getWidthOfSpace(), " ",
                                new int[]{' '}, last.getFont(), last.getFontSize(), (int) last.getFontSizeInPt());
                        allTextPositions.add(separatorSpace);
                    }
                }
                super.writeLineSeparator();
            }
        };

        stripper.setSortByPosition(true);
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        stripper.getText(document);

        final List<TextPositionSequence> hits = new ArrayList<TextPositionSequence>();
        TextPositionSequence word = new TextPositionSequence(allTextPositions);
        String string = word.toString();
        System.out.printf("  -- %s\n", string);

        int fromIndex = 0;
        int index;
        while ((index = string.indexOf(searchTerm, fromIndex)) > -1) {
            hits.add(word.subSequence(index, index + searchTerm.length()));
            fromIndex = index + 1;
        }

        return hits;
    }

}
