# Comprehensive Backend Services for Decentralized Exchanges on TON Blockchain

The API-DEX-TON-Blockchain project, hosted on GitHub, represents a sophisticated and comprehensive backend platform specifically designed for decentralized exchanges (DEX) on the TON blockchain. This initiative aims to deliver a robust and versatile API, facilitating the development and seamless integration of decentralized financial services on the TON blockchain.

## Extensive Project Components

- **.github Directory:**
  - This directory houses the GitHub Actions configurations, instrumental in automating various aspects of the development cycle. A notable enhancement includes the upgrade of actions/checkout from version 3 to 4, reflecting our commitment to utilizing cutting-edge tools and practices.

- **Core Module:**
  - The Core module is the heart of the project, where the principal business logic resides. In recent updates, we've removed the v1 reserves, a strategic move to escalate the system's overall performance and security. This module serves as the backbone, supporting the core functionalities required by decentralized exchanges.

- **Observer Module:**
  - The Observer module is dedicated to monitoring blockchain activities, particularly focusing on transaction tracking. It's an essential component for maintaining the integrity and transparency of DEX operations on the TON blockchain.

- **REST-v2 and REST Interfaces:**
  - These modules comprise RESTful API interfaces, which have been recently upgraded with `org.springdoc:springdoc-openapi-kotlin`. This enhancement bolsters the integration and documentation of our APIs, making them more accessible and user-friendly for developers.

- **Ton-Indexer Module:**
  - The Ton-Indexer is a crucial module for indexing the data on the TON blockchain. It has been updated to version 0.2.14 of ton-kotlin, optimizing the efficiency and reliability of blockchain data processing and indexing operations. This module plays a pivotal role in ensuring that data retrieval and querying are both swift and accurate, essential for real-time and historical data analysis in decentralized exchanges.

## Important Project Updates

- **Strategic Removal of v1 Reserves:**

A key focus has been on enhancing the system's effectiveness and security. By removing the v1 reserves, we've streamlined operations, reducing unnecessary complexities and bolstering overall system resilience.

- **Gradle/wrapper and gradlew Initialization:**

The project has initiated the use of Gradle, a powerful build automation tool. This includes the setup of gradle/wrapper and gradlew, facilitating better dependency management and streamlined project builds. These tools are integral for maintaining a consistent build environment and managing project dependencies effectively.

- **GRenovate.json Implementation:**

To ensure continuous and automatic management of project dependencies, we have integrated Renovate. This tool automatically updates dependencies, keeping the project in sync with the latest and most secure libraries.

- **Enhancements in Build.gradle.kts and settings.gradle.kts:**

These files have been updated to incorporate modern development practices and tools. This includes better integration of Kotlin with our build scripts, ensuring a more efficient and robust development process.

## Guide to Getting Started

For developers interested in contributing to or utilizing this project:

- **Repository Cloning:**

Begin by cloning the repository to your local machine. This will give you access to all the modules and components.

- **Documentation and Familiarization:**

Dive into the comprehensive documentation provided. It's crucial to understand the functionality and structure of the various modules and components within the project.

- **Development Environment Setup:**

Follow the detailed instructions provided in the gradlew and gradle/wrapper files to set up your development environment. This step is essential to ensure that your development setup aligns with the project's requirements.

## Inviting Community Contributions

We are deeply committed to open-source collaboration and encourage contributions from the developer community. Whether it's bug fixes, enhancements, documentation improvements, or new feature development, your input is invaluable. Please adhere to our contribution guidelines to ensure a smooth collaboration process.

## License Details

This project is distributed under the MIT License. This permissive license grants extensive freedoms, allowing for both private and commercial use, modification, and distribution of the software. For full license details, please refer to the LICENSE document.

