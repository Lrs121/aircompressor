/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.airlift.compress.v3.snappy;

import java.util.Random;

import static com.google.common.base.Verify.verify;

class RandomGenerator
{
    public final byte[] data;
    public int position;

    public RandomGenerator(double compressionRatio)
    {
        // We use a limited amount of data over and over again and ensure
        // that it is larger than the compression window (32KB), and also
        // large enough to serve all typical value sizes we want to write.
        Random rnd = new Random(301);
        data = new byte[1048576 + 100];
        for (int i = 0; i < 1048576; i += 100) {
            // Add a short fragment that is as compressible as specified ratio
            System.arraycopy(compressibleData(rnd, compressionRatio, 100), 0, data, i, 100);
        }
    }

    public int getNextPosition(int length)
    {
        if (position + length > data.length) {
            position = 0;
            verify(length < data.length);
        }
        int result = position;
        position += length;
        return result;
    }

    private static byte[] compressibleData(Random random, double compressionRatio, int length)
    {
        int raw = (int) (length * compressionRatio);
        if (raw < 1) {
            raw = 1;
        }
        byte[] rawData = generateRandomData(random, raw);

        // Duplicate the random data until we have filled "length" bytes
        byte[] dest = new byte[length];
        for (int i = 0; i < length; ) {
            int chunkLength = Math.min(rawData.length, length - i);
            System.arraycopy(rawData, 0, dest, i, chunkLength);
            i += chunkLength;
        }
        return dest;
    }

    private static byte[] generateRandomData(Random random, int length)
    {
        byte[] rawData = new byte[length];
        for (int i = 0; i < rawData.length; i++) {
            rawData[i] = (byte) random.nextInt(256);
        }
        return rawData;
    }
}
