import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class NGramLibraryBuilder {
	public static class NGramMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
		int noGram;
		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();
			noGram = conf.getInt("noGram", 5);
		}

		// map method
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			line = line.trim().toLowerCase().replaceAll("[^a-z]", " ");

			//split by ' ', '\t'...ect
			String[] words = line.split("\\s+");
			if (words.length<2) {
				return;
			}

			//I love big data
			StringBuilder sb;
			for (int i = 0; i < words.length - 1; i++) {
				sb = new StringBuilder();
				sb.append(words[i]);
				for (int j = 1; i + j < words.length && j < noGram; j++) {
					sb.append(" ");
					sb.append(words[i+j]);
					context.write(new Text(sb.toString().trim()), new IntWritable(1));
				}
			}
		}
	}

	public static class NGramReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		// reduce method
		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable value : values) {
				sum += value.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}

}