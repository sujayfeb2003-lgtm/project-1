package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CourseEnrollmentEntity
import com.example.model.Course
import com.example.ui.components.CatalogCourseCard

@Composable
fun CatalogScreen(
  courses: List<Course>,
  enrollments: List<CourseEnrollmentEntity>,
  searchQuery: String,
  selectedCategory: String,
  selectedLevel: String,
  onSearchQueryChanged: (String) -> Unit,
  onCategorySelected: (String) -> Unit,
  onLevelSelected: (String) -> Unit,
  onCourseSelected: (Course) -> Unit,
  onStartCourse: (Course) -> Unit,
  modifier: Modifier = Modifier
) {
  val categories = listOf("All", "Mobile Dev", "AI & Data", "Design", "DevOps & Cloud")
  val levels = listOf("All", "Beginner", "Intermediate", "Advanced")

  val filteredCourses = courses.filter { course ->
    val matchesSearch = searchQuery.isBlank() ||
      course.title.contains(searchQuery, ignoreCase = true) ||
      course.description.contains(searchQuery, ignoreCase = true) ||
      course.tags.any { it.contains(searchQuery, ignoreCase = true) }

    val matchesCategory = selectedCategory == "All" || course.category.equals(selectedCategory, ignoreCase = true)
    val matchesLevel = selectedLevel == "All" || course.level.equals(selectedLevel, ignoreCase = true)

    matchesSearch && matchesCategory && matchesLevel
  }

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("catalog_screen")
  ) {
    // 1. Header
    item {
      Column {
        Text(
          text = "Explore Masterclasses",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = (-0.3).sp,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Interactive video lessons with hands-on live checkpoints",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 2.dp)
        )
      }
    }

    // 2. Search Input (Sleek pill style)
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        placeholder = { Text("Search topics, Kotlin, AI, Kubernetes...") },
        leadingIcon = {
          Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChanged("") }) {
              Icon(Icons.Filled.Clear, contentDescription = "Clear search")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(50.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth().testTag("catalog_search_input")
      )
    }

    // 3. Category Filter Chips (Pill shapes)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Category",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(categories) { cat ->
            val isSelected = selectedCategory == cat
            FilterChip(
              selected = isSelected,
              onClick = { onCategorySelected(cat) },
              label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
              ),
              shape = RoundedCornerShape(50.dp),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isSelected,
                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
              ),
              modifier = Modifier.testTag("filter_category_$cat")
            )
          }
        }
      }
    }

    // 4. Level Filter Chips (Pill shapes)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Difficulty Level",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(levels) { level ->
            val isSelected = selectedLevel == level
            FilterChip(
              selected = isSelected,
              onClick = { onLevelSelected(level) },
              label = { Text(level, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
              ),
              shape = RoundedCornerShape(50.dp),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isSelected,
                borderColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
              ),
              modifier = Modifier.testTag("filter_level_$level")
            )
          }
        }
      }
    }

    // 5. Results Counter
    item {
      Text(
        text = "Showing ${filteredCourses.size} courses",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // 6. Course List
    if (filteredCourses.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp).fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Filled.FilterList,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "No matching courses found",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Try adjusting your search query or category filters",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(filteredCourses, key = { it.id }) { course ->
        val enrollment = enrollments.find { it.courseId == course.id }
        val isEnrolled = enrollment != null
        val progress = enrollment?.progressPercent ?: 0f

        CatalogCourseCard(
          course = course,
          isEnrolled = isEnrolled,
          progressPercent = progress,
          onCardClick = { onCourseSelected(course) },
          onStartClick = { onStartCourse(course) }
        )
      }
    }
  }
}
