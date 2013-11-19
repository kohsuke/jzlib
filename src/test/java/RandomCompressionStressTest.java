import com.jcraft.jzlib.DeflaterOutputStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.zip.InflaterOutputStream;

/**
 * @author Kohsuke Kawaguchi
 */
public class RandomCompressionStressTest extends Assert {
    @Test
    public void compressionStressTest() throws Exception {
        Random r = new Random(0);

        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        DeflaterOutputStream o = new DeflaterOutputStream(new InflaterOutputStream(dest));
        o.setSyncFlush(true);

        ByteArrayOutputStream mirror = new ByteArrayOutputStream();

        long total=0;

        for (int i=0; i<1024*1024; i++) {
            int len = r.nextInt(4096);
            int start = r.nextInt(256);
            int extra = r.nextInt(256);

            byte[] buf = new byte[start+len+extra];
            r.nextBytes(buf);
            // reduce the randomness to allow gzip to compress
            for (int j=0; j<len; j++)
                buf[start+j] &= 0xF0;

            mirror.write(buf,start,len);
            o.write(buf,start,len);
            total += len;

            if (r.nextInt(128)==0) {
                System.out.printf("%12d\n",total);
                o.flush();
                check(dest, mirror);
            }
        }

        o.close();
        check(dest, mirror);
    }

    private void check(ByteArrayOutputStream dest, ByteArrayOutputStream mirror) {
        assertArrayEquals(dest.toByteArray(),mirror.toByteArray());
        mirror.reset();
        dest.reset();
    }
}
