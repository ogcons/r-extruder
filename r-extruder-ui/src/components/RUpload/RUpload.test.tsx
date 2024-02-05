import React from "react";
import { render, fireEvent, waitFor } from "@testing-library/react";
import axios from "axios";
import RUpload from "./RUpload";

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("RUpload", () => {
  beforeAll(() => {
    // Mock window.URL.createObjectURL
    Object.defineProperty(window.URL, "createObjectURL", { value: jest.fn() });
  });

  it("renders correctly", () => {
    const { getByText, getByLabelText } = render(<RUpload />);

    expect(getByText("Upload your R files:")).toBeInTheDocument();
    expect(
      getByLabelText("Select your returning parameter")
    ).toBeInTheDocument();
    expect(getByText("Submit")).toBeInTheDocument();
  });

  it("submits form and sets S3 key on successful response", async () => {
    mockedAxios.post.mockResolvedValue(Promise.resolve({ data: {} }));

    const { getByText, getByLabelText } = render(<RUpload />);

    const fileInput = getByText("Upload your R files:");
    getByLabelText("Select your returning parameter");
    const submitButton = getByText("Submit");

    fireEvent.change(fileInput, {
      target: { files: [new File(["file"], "file.R")] },
    });
    fireEvent.click(submitButton);

    await waitFor(async () => {
      const s3KeyElement = getByText("Your S3 key for download:");
      expect(s3KeyElement).toBeInTheDocument();
    });
  });
});
