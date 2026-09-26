package com.learnpointer.courses;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TutorialContentService {

	private final Path contentDirectory;
	private final ObjectMapper objectMapper;

	public TutorialContentService(@Value("${content.directory:content}") String contentDirectory) {
		this.contentDirectory = Path.of(contentDirectory).toAbsolutePath().normalize();
		this.objectMapper = new ObjectMapper();
	}

	public List<ContentGroup> findContentGroups() {
		try (Stream<Path> sections = Files.list(contentDirectory)) {
			return sections.filter(Files::isDirectory)
					.flatMap(this::courseDirectories)
					.map(this::toContentGroup)
					.sorted(Comparator.comparing(ContentGroup::title))
					.toList();
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Content folders could not be read", exception);
		}
	}

	public List<ContentSection> findContentSections() {
		try (Stream<Path> sections = Files.list(contentDirectory)) {
			return sections.filter(Files::isDirectory)
					.map(this::toContentSection)
					.sorted(Comparator.comparing(ContentSection::title))
					.toList();
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Content sections could not be read", exception);
		}
	}

	public List<ContentCourse> findCourses(String sectionSlug) {
		Path section = sectionDirectoryFor(sectionSlug);
		try (Stream<Path> courses = Files.list(section)) {
			return courses.filter(Files::isDirectory)
					.map(this::toContentCourse)
					.sorted(Comparator.comparing(ContentCourse::title))
					.toList();
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Courses could not be read", exception);
		}
	}

	public ContentGroup findContentGroup(String sectionSlug, String courseSlug) {
		return toContentGroup(directoryFor(sectionSlug, courseSlug));
	}

	public List<Tutorial> findAll() {
		return findAll("solid-principles");
	}

	public List<Tutorial> findAll(String courseSlug) {
		return findAll(directoryFor(courseSlug));
	}

	public List<Tutorial> findAll(String sectionSlug, String courseSlug) {
		return findAll(directoryFor(sectionSlug, courseSlug));
	}

	private List<Tutorial> findAll(Path directory) {
		try (Stream<Path> files = Files.list(directory)) {
			return files.filter(Files::isRegularFile)
					.filter(this::isSupportedImage)
					.map(this::toTutorial)
					.sorted(Comparator.comparing(Tutorial::title))
					.toList();
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Tutorial content could not be read", exception);
		}
	}

	public Tutorial findBySlug(String slug) {
		return findBySlug("solid-principles", slug);
	}

	public Tutorial findBySlug(String courseSlug, String slug) {
		return findAll(courseSlug).stream()
				.filter(tutorial -> tutorial.slug().equals(slug))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tutorial not found"));
	}

	public Tutorial findBySlug(String sectionSlug, String courseSlug, String slug) {
		return findAll(sectionSlug, courseSlug).stream()
				.filter(tutorial -> tutorial.slug().equals(slug))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tutorial not found"));
	}

	public Resource imageFor(String slug) {
		return imageFor("solid-principles", slug);
	}

	public Resource imageFor(String courseSlug, String slug) {
		Tutorial tutorial = findBySlug(courseSlug, slug);
		try {
			Resource resource = new UrlResource(directoryFor(courseSlug).resolve(tutorial.fileName()).toUri());
			if (!resource.exists() || !resource.isReadable()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tutorial image not found");
			}
			return resource;
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tutorial image not found", exception);
		}
	}

	public Resource imageFor(String sectionSlug, String courseSlug, String slug) {
		Tutorial tutorial = findBySlug(sectionSlug, courseSlug, slug);
		return imageFor(directoryFor(sectionSlug, courseSlug), tutorial);
	}

	public Resource contentFile() {
		return contentFile("solid-principles");
	}

	public Resource contentFile(String courseSlug) {
		return contentFile(directoryFor(courseSlug));
	}

	public Resource contentFile(String sectionSlug, String courseSlug) {
		return contentFile(directoryFor(sectionSlug, courseSlug));
	}

	private Resource contentFile(Path directory) {
		try {
			Resource resource = new UrlResource(directory.resolve("content.json").toUri());
			if (!resource.exists() || !resource.isReadable()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tutorial content file not found");
			}
			return resource;
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tutorial content file not found", exception);
		}
	}

	private Path directoryFor(String courseSlug) {
		try (Stream<Path> sections = Files.list(contentDirectory)) {
			return sections.filter(Files::isDirectory)
					.flatMap(this::courseDirectories)
					.filter(course -> toSlug(course.getFileName().toString()).equals(courseSlug))
					.findFirst()
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content folder not found"));
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Content folders could not be read", exception);
		}
	}

	private Path directoryFor(String sectionSlug, String courseSlug) {
		Path directory = courseDirectories(sectionDirectoryFor(sectionSlug))
				.filter(course -> toSlug(course.getFileName().toString()).equals(courseSlug))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content folder not found"));
		if (!directory.startsWith(contentDirectory)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid content path");
		}
		return directory;
	}

	private Path sectionDirectoryFor(String sectionSlug) {
		Path section;
		try (Stream<Path> sections = Files.list(contentDirectory)) {
			section = sections.filter(Files::isDirectory)
					.filter(candidate -> toSlug(candidate.getFileName().toString()).equals(sectionSlug))
					.findFirst()
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content section not found"));
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Content folders could not be read", exception);
		}
		if (!section.startsWith(contentDirectory)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid content path");
		}
		return section;
	}

	private ContentSection toContentSection(Path section) {
		return new ContentSection(toSlug(section.getFileName().toString()),
				titleFromSlug(section.getFileName().toString()), findCourses(toSlug(section.getFileName().toString())));
	}

	private ContentCourse toContentCourse(Path directory) {
		ContentDocument document = readContentDocument(directory);
		return new ContentCourse(toSlug(directory.getFileName().toString()), document.title(), document.description());
	}

	private ContentGroup toContentGroup(Path directory) {
		ContentDocument document = readContentDocument(directory);
		List<ContentItem> tutorials = document.tutorials() != null ? document.tutorials()
				: findAll(directory).stream()
						.map(tutorial -> new ContentItem(tutorial.slug(), tutorial.title(), "",
								"/api/content/" + toSlug(directory.getParent().getFileName().toString()) + "/"
										+ toSlug(directory.getFileName().toString()) + "/" + tutorial.slug() + "/image"))
						.toList();
		return new ContentGroup(toSlug(directory.getParent().getFileName().toString()),
				toSlug(directory.getFileName().toString()), document.title(), document.description(), tutorials);
	}

	private ContentDocument readContentDocument(Path directory) {
		Path contentFile = directory.resolve("content.json");
		try {
			return Files.exists(contentFile)
					? objectMapper.readValue(contentFile.toFile(), ContentDocument.class)
					: new ContentDocument(titleFromSlug(directory.getFileName().toString()), "", null);
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Content metadata could not be read for " + directory.getFileName(), exception);
		}
	}

	private Stream<Path> courseDirectories(Path section) {
		try (Stream<Path> courses = Files.list(section)) {
			return courses.filter(Files::isDirectory).toList().stream();
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Content section could not be read", exception);
		}
	}

	private Resource imageFor(Path directory, Tutorial tutorial) {
		try {
			Resource resource = new UrlResource(directory.resolve(tutorial.fileName()).toUri());
			if (!resource.exists() || !resource.isReadable()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tutorial image not found");
			}
			return resource;
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tutorial image not found", exception);
		}
	}

	private String titleFromSlug(String slug) {
		return Stream.of(slug.replace('-', ' ').split(" "))
				.map(word -> word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1))
				.reduce((left, right) -> left + " " + right).orElse(slug);
	}

	private Tutorial toTutorial(Path path) {
		String title = removeExtension(path.getFileName().toString());
		return new Tutorial(toSlug(title), title, path.getFileName().toString());
	}

	private boolean isSupportedImage(Path path) {
		String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
		return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
				|| fileName.endsWith(".gif") || fileName.endsWith(".webp");
	}

	private String removeExtension(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

	private String toSlug(String title) {
		return title.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
	}
}