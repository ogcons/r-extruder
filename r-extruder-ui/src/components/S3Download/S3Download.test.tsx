import React from "react";
import { render, fireEvent } from "@testing-library/react";
import axios from "axios";
import S3Download from "./S3Download";

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("S3Download", () => {
  beforeAll(() => {
    // Mock window.URL.createObjectURL
    Object.defineProperty(window.URL, "createObjectURL", { value: jest.fn() });
  });

  it("renders correctly", () => {
    const { getByText, getByPlaceholderText } = render(<S3Download />);
    expect(getByText("Download your Word document")).toBeInTheDocument();
    expect(getByPlaceholderText("Enter S3 key")).toBeInTheDocument();
    expect(getByText("Download")).toBeInTheDocument();
  });

  it("calls the download function when the button is clicked", async () => {
    const { getByText, getByPlaceholderText } = render(<S3Download />);
    const input = getByPlaceholderText("Enter S3 key");
    const button = getByText("Download");

    mockedAxios.get.mockResolvedValueOnce({
      data: "mockData",
      headers: { "content-type": "mockType" },
    });

    fireEvent.change(input, { target: { value: "testKey" } });
    fireEvent.click(button);

    expect(mockedAxios.get).toHaveBeenCalledWith(
      "http://localhost:8080/api/extractors/s3/testKey",
      { responseType: "arraybuffer" }
    );
  });
});
