package com.curiousattemptbunny.jai.chess

import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import javax.media.jai.Histogram
import javax.media.jai.KernelJAI
import java.awt.Canvas
import javax.media.jai.PlanarImage
import javax.media.jai.PerspectiveTransform
import javax.media.jai.WarpPerspective
import java.awt.image.renderable.ParameterBlock
import javax.media.jai.JAI
import javax.swing.JFrame
import javax.media.jai.Interpolation

/**
 * Created by IntelliJ IDEA.
 * User: merlyna
 * Date: 9/9/11
 * Time: 9:06 PM
 * To change this template use File | Settings | File Templates.
 */
class MetaMethods {
    MetaMethods() {
        PlanarImage.metaClass.static.getCapture = {
            new File('capture.png').delete()
            "./wacaw --png capture".execute().waitFor()
            new File('capture.png').image
        }

        File.metaClass.getImage = {
            return JAI.create("fileload", delegate.absolutePath)
        }

        PlanarImage.metaClass.getWrite = {
            JAI.create("filestore", delegate, new File(home, "step"+next+".png").absolutePath)
            next++
            delegate
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
            g.metaClass.drawString = { str, a -> delegate.drawString(str, a.x, a.y) }

            if (drawClosure.maximumNumberOfParameters == 2) {
                drawClosure(canvas, g)
            } else {
                drawClosure(g)
            }
        }

        PlanarImage.metaClass.toSquare = { width, height, corners ->
            PerspectiveTransform transform = PerspectiveTransform.getQuadToQuad(
                    0, 0,
                    width, 0,
                    width, height,
                    0, height,
                    corners[3].x, corners[3].y,
                    corners[2].x, corners[2].y,
                    corners[1].x, corners[1].y,
                    corners[0].x, corners[0].y
            )

            WarpPerspective wp = new WarpPerspective(transform);

            def pb = (new ParameterBlock()).addSource(delegate);
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
    }
}
