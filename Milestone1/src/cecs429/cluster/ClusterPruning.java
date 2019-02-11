package cecs429.cluster;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.commons.collections4.CollectionUtils;

public class ClusterPruning {

	/**
	 * This method is used to calculate the similarity between a leader document and a non-leader document
	 * @param leaderDoc - A leader document
	 * @param otherDoc - The other document used for comparison
	 * @return similarityScore - Returns the cosine similarity score between the two documents.
	 */
	public double calculateDistance(ClusterDoc leaderDoc, ClusterDoc otherDoc) {
		HashMap<String, Double> leaderMap = leaderDoc.getTermWdtMap();
		HashMap<String, Double> otherMap = otherDoc.getTermWdtMap();
		Double similarityScore = 0.0;
		for (String leaderTerm : leaderMap.keySet()) {
			if (otherMap.containsKey(leaderTerm)) {
				similarityScore += (leaderMap.get(leaderTerm) * otherMap.get(leaderTerm));
			}
		}
		similarityScore = (similarityScore) / (leaderDoc.getLd() * otherDoc.getLd());
		return similarityScore;
	}

	/**
	 * This method is used to create a cluster map which contains a leader document and a set of followers
	 * @param leaderDocumentsMap - A map containing all the leader documents along with their values like Ld, AvgTfd, docLength, and docSize in ClusterDoc object
	 * @param otherDocumentsMap - A map of other documents with similar details.
	 * @param path - Path where the map will be stored on disk.
	 */
	public void formClusters(HashMap<Integer, ClusterDoc> leaderDocumentsMap,
			HashMap<Integer, ClusterDoc> otherDocumentsMap, String path) {
		Double similarityScore = 0.0;
		HashMap<Integer, HashSet<Integer>> clusterMap = new HashMap<Integer, HashSet<Integer>>();

		for (Integer otherDocID : otherDocumentsMap.keySet()) {
			HashMap<Integer, Double> leaderScoreMap = new HashMap<Integer, Double>();
			for (Integer leaderDocID : leaderDocumentsMap.keySet()) {
				similarityScore = calculateDistance(otherDocumentsMap.get(otherDocID),
						leaderDocumentsMap.get(leaderDocID));
				leaderScoreMap.put(leaderDocID, similarityScore);
			}

			PriorityQueue<Map.Entry<Integer, Double>> heap = new PriorityQueue<Map.Entry<Integer, Double>>(
					new Comparator<Map.Entry<Integer, Double>>() {
						@Override
						public int compare(Map.Entry<Integer, Double> entry1, Map.Entry<Integer, Double> entry2) {
							if (entry2.getValue() > entry1.getValue())
								return 1;
							else
								return -1;
						}
					});
			for (Map.Entry<Integer, Double> entry : leaderScoreMap.entrySet()) {
				heap.offer(entry);
			}
			// get leader with highest similarity score to the compared document
			Map.Entry<Integer, Double> entry = heap.poll();
			if (clusterMap.containsKey(entry.getKey())) {
				// if cluster contains the leader document ID
				HashSet<Integer> followerDocs = new HashSet<Integer>();
				// get the follower documents
				followerDocs = clusterMap.get(entry.getKey());
				// append this document to other documents
				followerDocs.add(otherDocID);
				// put this in the map
				clusterMap.put(entry.getKey(), followerDocs);
			} else { // it does not contain the leader document
				// create a list that will contain the follower documents
				HashSet<Integer> followerDocs = new HashSet<Integer>();
				// add this document to the list
				followerDocs.add(otherDocID);
				// put this in the map
				clusterMap.put(entry.getKey(), followerDocs);
			}
		}

		// write out the other leaders as lonely :(
		for (Integer leaderDocID : leaderDocumentsMap.keySet()) {
			if (!clusterMap.containsKey(leaderDocID)) {
				clusterMap.put(leaderDocID, null);
			}
		}
		writeClusters(path, clusterMap);
	}

	/**
	 * This method is used to write the clusters on to the disk for future reference.
	 * @param path - Path on disk where it is to be written 
	 * @param clusterMap - The map containing the leader document IDs as keys and HashSet of integers as followers.
	 */
	public void writeClusters(String path, HashMap<Integer, HashSet<Integer>> clusterMap) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(path + "//similarity.bin", "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			int numberOfLeaders = clusterMap.size();
			System.out.println("Number of Leaders:" + numberOfLeaders);
			raf.writeInt(numberOfLeaders);

			for (Integer leaderID : clusterMap.keySet()) {
				raf.writeInt(leaderID);
				// System.out.println("Leader:" + leaderID);
				HashSet<Integer> followers = clusterMap.get(leaderID);
				if (CollectionUtils.isEmpty(followers)) {
					raf.writeInt(0);
				} else {
					raf.writeInt(followers.size());
					for (Integer followerID : followers) {
						raf.writeInt(followerID);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * This method is used to read clusters from the disk
	 * @param path - Path where the index is stored on disk.
	 * @return clusterMap - The map containing the leader document IDs as keys and HashSet of integers as followers.
	 */
	public HashMap<Integer, HashSet<Integer>> readClusters(String path) {
		HashMap<Integer, HashSet<Integer>> clusterMap = new HashMap<Integer, HashSet<Integer>>();
		int numberOfLeaders = 0;
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(path + "//similarity.bin", "r");
			numberOfLeaders = raf.readInt();
			for (int i = 0; i < numberOfLeaders; i++) {
				int leaderDocID = raf.readInt();

				HashSet<Integer> followerDocIds = new HashSet<Integer>();
				int numberOfFollowers = raf.readInt();
				for (int j = 0; j < numberOfFollowers; j++) {
					followerDocIds.add(raf.readInt());
				}
				clusterMap.put(leaderDocID, followerDocIds);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("error read from similarity");
			e.printStackTrace();
		}
		return clusterMap;
	}
}
