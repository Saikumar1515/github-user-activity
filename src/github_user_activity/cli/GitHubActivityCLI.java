package github_user_activity.cli;

import java.io.*;
import java.net.*;

class GitHubActivityCLI {

	private static final String GITHUB_API_URL = "https://api.github.com/users/";

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Usage: github-activity <username>");
			return;
		}

		String username = args[0];
		fetchAndDisplayActivity(username);
	}

	private static void fetchAndDisplayActivity(String username) {
		try {
			URL url = new URL(GITHUB_API_URL + username + "/events");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");

			int responseCode = connection.getResponseCode();
			if (responseCode != 200) {
				System.out.println("Error: Unable to fetch activity. (HTTP " + responseCode + ")");
				return;
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();

			String json = response.toString();
			if (json.equals("[]")) {
				System.out.println("No recent activity found for user: " + username);
				return;
			}

			
			String[] events = json.split("\\},\\{");
			for (String event : events) {
				String type = extractValue(event, "\"type\":\"");
				String repo = extractValue(event, "\"name\":\"");

				if (type == null || repo == null)
					continue;

				switch (type) {
				case "PushEvent":
					System.out.println("Pushed commits to " + repo);
					break;
				case "IssuesEvent":
					System.out.println("Opened a new issue in " + repo);
					break;
				case "WatchEvent":
					System.out.println("Starred " + repo);
					break;
				case "ForkEvent":
					System.out.println("Forked " + repo);
					break;
				default:
					System.out.println("Performed " + type + " on " + repo);
				}
			}

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	private static String extractValue(String text, String key) {
		int start = text.indexOf(key);
		if (start == -1)
			return null;
		start += key.length();
		int end = text.indexOf("\"", start);
		if (end == -1)
			return null;
		return text.substring(start, end);
	}
}
