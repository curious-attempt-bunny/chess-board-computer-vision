package com.curiousattemptbunny.jai.chess

import java.awt.Canvas
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.renderable.ParameterBlock
import javax.media.jai.operator.OrDescriptor
import javax.media.jai.operator.SubtractDescriptor
import javax.media.jai.operator.XorDescriptor
import javax.swing.JFrame
import javax.media.jai.*

File home = new File("/Users/user/Documents/fun/groovy/chess")
File data = new File(home, "data")
File input = new File(data, "Photo 21.jpg")
File output = new File(home, "generated/normalized1.png")

def inputs = [new File(data, "Photo 21.jpg"), new File(data, "Photo 22.jpg")]

File.metaClass.getImage = {
    return JAI.create("fileload", delegate.absolutePath)
}

PlanarImage.metaClass.getGrayScale = {
     ParameterBlock pb = new ParameterBlock();
     pb.addSource(delegate);
     pb.add([[ 0.114D, 0.587D, 0.299D, 0.0D ]] as double[][]);

     def dst = (PlanarImage)JAI.create("bandcombine", pb, null);

    return dst
}

PlanarImage.metaClass.show = { drawClosure ->
    JFrame jFrame = new JFrame();
    jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jFrame.setResizable(false);
    jFrame.setFocusTraversalKeysEnabled(false);

    Canvas canvas = new Canvas();
    canvas.setSize(delegate.width, delegate.height);
    canvas.setIgnoreRepaint(true);

    jFrame.getContentPane().add(canvas);
    jFrame.pack();
    jFrame.show();

    Graphics2D g = canvas.graphics

    g.drawRenderedImage(delegate, new AffineTransform())

    g.metaClass.drawLine = { a, b -> delegate.drawLine(a.x, a.y, b.x, b.y) }

    drawClosure(g)
}

PlanarImage.metaClass.toSquare = { width, height, corners ->
    PerspectiveTransform transform = PerspectiveTransform.getQuadToQuad(
            0, 0,
            width, 0,
            width, height,
            0, height,
            corners[0].x, corners[0].y,
            corners[1].x, corners[1].y,
            corners[3].x, corners[3].y,
            corners[2].x, corners[2].y )

    WarpPerspective wp = new WarpPerspective(transform);

    pb = (new ParameterBlock()).addSource(delegate);
    pb.add(wp);
    pb.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC));

    PlanarImage result = (PlanarImage)JAI.create("warp",pb);

    pb = new ParameterBlock();
    pb.addSource(result);
    pb.add((float)0);
    pb.add((float)0);
    pb.add((float)width);
    pb.add((float)height);
    result = JAI.create("crop",pb);

    result
}

PlanarImage.metaClass.getEdgeDetect = {
     float[] data_h = [ 1.0F,   0.0F,   -1.0F,
                        1.414F, 0.0F,   -1.414F,
                        1.0F,   0.0F,   -1.0F ] as float[];
     float[] data_v = [-1.0F,  -1.414F, -1.0F,
                        0.0F,   0.0F,    0.0F,
                        1.0F,   1.414F,  1.0F] as float[]

     KernelJAI kern_h = new KernelJAI(3,3,data_h);
     KernelJAI kern_v = new KernelJAI(3,3,data_v);

     return JAI.create("gradientmagnitude", delegate,
                                      kern_h, kern_v);
}

PlanarImage.metaClass.minus = { rhs ->
    SubtractDescriptor.create(delegate, rhs, null)
}

PlanarImage.metaClass.getDilate = {
    JAI.create("dilate", delegate, new KernelJAI(3, 3, [0,10,0, 10,10,10, 0,10,0] as float[]))
}

PlanarImage.metaClass.getErode = {
    JAI.create("erode", delegate, new KernelJAI(3, 3, [0,10,0, 10,10,10, 0,10,0] as float[]))
}

PlanarImage.metaClass.getOpen = {
    delegate.erode.dilate
}

PlanarImage.metaClass.getBinarize = {
    Histogram histogram =
        (Histogram)JAI.create("histogram", delegate).getProperty("histogram");

    def source = delegate

    def bin = { cut ->
        double[] threshold
        threshold = histogram.getPTileThreshold(cut);
        JAI.create("binarize", source, new Double(threshold[0]));
    }

    (bin(0.7) - bin(0.95)).or (bin(0.05) - bin(0.3))
}

PlanarImage.metaClass.xor = { rhs ->
    XorDescriptor.create(delegate, rhs, null)
}

PlanarImage.metaClass.or = { rhs ->
    OrDescriptor.create(delegate, rhs, null)
}


def coords = [[x:22, y:412], [x:620, y:406], [x:150, y:171], [x:488, y:167]]

/*
image.toBlackAndWhite().show { g ->
    g.setColor(Color.RED)
    g.drawLine(coords[0], coords[1])
    g.drawLine(coords[0], coords[2])
    g.drawLine(coords[1], coords[3])
    g.drawLine(coords[2], coords[3])
}
*/

def highlight = { square ->
    return { g ->
        g.setColor(Color.RED)
        (0..square.length()-1).step(2) { i ->
            int y = (7-(square[i].toCharacter().charValue()-'a'.toCharacter().charValue())) *(480/8)
            int x = (7-(square[i+1].toCharacter().charValue()-'1'.toCharacter().charValue())) *(480/8)
            g.drawRect(x, y, (int)(480/8), (int)(480/8))
        }
    }
}

next = 1

PlanarImage.metaClass.getWrite = {
    JAI.create("filestore", delegate, new File(home, "step"+next+".png").absolutePath)
    next++
    delegate
}

inputs = inputs.collect { it.image.grayScale.toSquare(480, 480, coords) }
inputs[0].show highlight('e2')
inputs[1].show highlight('e4')
inputs = inputs.collect { it.binarize }
inputs[0].show highlight('e2')
inputs[1].show highlight('e4')
inputs[1].xor(inputs[0]).erode.open.show highlight('e2e4')

