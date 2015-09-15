package main.groovy


import com.craigburke.document.builder.PdfDocumentBuilder
import com.craigburke.document.core.Align

/**
 * Created by anandu on 7/8/15.
 */
class GPdfBuilder {
    private def pdfBuilder
    private def pdfStream
    private def templateQueue = new LinkedList<Closure>()
    protected def headerImage = new File("groovy-logo.png")
    def defaultDocStyle = {
        'document' font: [family: 'Arial', size: 9.pt, color: '#000000']
    }

    public GPdfBuilder(OutputStream stream) {
        this.pdfStream = stream
        this.pdfBuilder = new PdfDocumentBuilder(this.pdfStream)
    }

    public def insert(params) {
        def template = params.template.rehydrate(this.pdfBuilder, null, null)
        template.resolveStrategy = Closure.DELEGATE_ONLY
        this.templateQueue.push([(template): params.data])
        this
    }

    public def buildPdf() {
        this.pdfBuilder.create {
            document(template: defaultDocStyle, header: { info ->
                paragraph(align: Align.LEFT) {
                    image data: headerImage.bytes, width: 250 * 0.50, height: 125 * 0.50
                }
            }, footer: { info ->
                table(border: [size: 0]) {
                    row {
                        cell "Date Generated: ${info.dateGenerated.format('yyyy-MM-dd hh:mm a')}"
                        cell "Page ${info.pageNumber} of ${info.pageCount}", align: 'right'
                    }
                }
            }) {
                while (!this.templateQueue.empty) {
                    def currentRenderInstructions = this.templateQueue.poll()
                    def renderTemplate = currentRenderInstructions.keySet()?.find { true }
                    def context = currentRenderInstructions.values()?.find { true }
                    renderTemplate(context)
                }
            }
        }
    }

}

def pdfTemplate = { context ->
    paragraph(align: Align.LEFT) {
        text "Test", font: [bold: true]
        lineBreak()
        text "Dummy", font: [bold: true]
        lineBreak()
        text "Test", font: [italic: true, size: 8.pt]
    }
    if (context.size() > 0) {
        paragraph(align: Align.LEFT) {
            text "Data ", font: [bold: true]
            text "TestData"
        }

        table(border: [size: 0], margin: [top: 1.0.inches], columns: [1, 4, 1, 1]) {
            def offset = 1
            context.eachWithIndex { data, index ->
                row {
                    cell(align: Align.CENTER) {
                        text "${index + offset}"
                    }
                    cell {
                        text "table1 cell2"
                    }
                    cell(align: Align.RIGHT) {
                        text "table1 cell3"
                    }
                    cell(align: Align.RIGHT) {
                        text "table1 cell4"
                    }
                }
            }
        }
    }
    table(border: [size: 0], margin: [top: 1.0.inches], columns: [1, 4]) {
        row {
            cell(align: Align.LEFT) {
                text "Table 2 cell1"
            }
            cell(align: Align.LEFT) {
                text "Table 2 cell2"
            }
        }
    }
    table(border: [size: 0], margin: [top: 1.0.inches], columns: [1, 5]) {
        row {
            cell(align: Align.LEFT) {
                text "Table3 cell1 "
            }
            cell(align: Align.LEFT) {
                text "${context.collect().join(",")}"
            }
        }
    }
}


def anotherPdfTemplate = { context ->
    paragraph {
        font.size = 9.pt
        text "Another name", font: [size: 14.pt]
        lineBreak()
        text "Another Dummy"
        text "Another text"
        lineBreak()
        text "${context.collect().join(",")}"
        lineBreak()
        text "___________________________________________________________________________"
    }
}

ArrayList dummyData = new ArrayList<String>()
ArrayList dummyLessData = new ArrayList<String>()
ArrayList dummyMoreLessData = new ArrayList<String>()
def stream = new FileOutputStream('build.pdf', false)
def builder = new GPdfBuilder(stream)
20.times {
    dummyData.add("dummyData${it}")
}
15.times {
    dummyLessData.add("dummyData${it}")
}
10.times {
    dummyMoreLessData.add("dummyData${it}")
}
2.times {
    builder.insert template: pdfTemplate, data: dummyData
}
2.times {
    builder.insert template: anotherPdfTemplate, data: dummyData
}
2.times {
    builder.insert template: pdfTemplate, data: dummyLessData
}
2.times {
    builder.insert template: anotherPdfTemplate, data: dummyMoreLessData
}
2.times {
    builder.insert template: pdfTemplate, data: dummyMoreLessData
}
builder.insert template: anotherPdfTemplate, data: dummyMoreLessData
2.times {
    builder.insert template: pdfTemplate, data: dummyData
}
builder.insert template: anotherPdfTemplate, data: dummyMoreLessData
builder.buildPdf()


